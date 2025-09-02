package com.gbft.framework.core;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dataset {

    protected Map<Integer, AtomicInteger> records;
    protected Map<Integer, Long> recordCurrentVersion;
    public Map<Integer, Long> recordLatestVersion;
    public Entity entity;

    public static final int DEFAULT_VALUE = 1000;
    public static final int RECORD_COUNT = Config.integer("workload.dataset-size");

    public Dataset() {
        records = DataUtils.concurrentMapWithDefaults(RECORD_COUNT, x -> new AtomicInteger(DEFAULT_VALUE));
        recordCurrentVersion = new TreeMap<>();
        recordLatestVersion = new TreeMap<>();
    }

    public Dataset(Entity entity) {
        this();
        this.entity = entity;
    }

    // Copy constructor
    public Dataset(Dataset dataset) {
        records = new ConcurrentHashMap<>();
        for (var entry : dataset.records.entrySet()) {
            records.put(entry.getKey(), new AtomicInteger(entry.getValue().get()));
        }
        recordCurrentVersion = new TreeMap<>();
        recordLatestVersion = new TreeMap<>();
    }

    public void setRecords(Map<Integer, Integer> map) {
        records.clear();
        for (var entry : map.entrySet()) {
            records.put(entry.getKey(), new AtomicInteger(entry.getValue()));
        }
    }

    public Map<Integer, AtomicInteger> getRecords() {
        return records;
    }

    public int execute(RequestData request) {
        // Track stats
        entity.updateKeyAccessesInEpisode(request.getReadSetList());
        entity.updateKeyAccessesInEpisode(request.getWriteSetList());

        if(request.getWriteSetCount() > 0) {
            entity.addWriteTransactionsCount(1);
            entity.addTotalTransactionsCount(1);
        }
        else{
            entity.addTotalTransactionsCount(1);
        }
        // entity.addWriteTransactionsCount(request.getWriteSetCount());
        // entity.addTotalTransactionsCount(request.getWriteSetCount() + request.getReadSetCount());

        List<Integer> values = new ArrayList<>();

        for (var op : request.getWriteSetList()) {
            runComputeDummy(request);
            values.add(processRequest(op));
        }

        for (var op : request.getReadSetList()) {
            values.add(processRequest(op));
        }

        return values.get(0);
    }

    public void runComputeDummy(RequestData request) {
        if (request.getComputeFactor() > 0) {
            int dummyCounter = 0;
            var random = new Random();
            for (int i = 0; i < request.getComputeFactor(); i++) {
                dummyCounter += random.nextInt();
            }
            try {
                OutputStream.nullOutputStream().write(dummyCounter);
            } catch (IOException ignored) {}
        }
    }

    public int processRequest(OperationSet operation) {
        AtomicInteger counter = records.computeIfAbsent(
            operation.getRecord(),
            x -> new AtomicInteger(DEFAULT_VALUE)
        );

        return switch (operation.getOp()) {
            case ADD -> counter.addAndGet(operation.getValue());
            case SUB -> counter.addAndGet(-operation.getValue());
            case INC -> counter.incrementAndGet();
            case DEC -> counter.decrementAndGet();
            default -> counter.get();
        };
    }

    /**
     * Sets a single record to a specific value and bumps its version.
     */
    public void update(RequestData request, int value) {
        if (request.getWriteSetList().isEmpty()) {
            return;
        }

        int record = request.getWriteSetList().getFirst().getRecord();
        AtomicInteger counter = records.computeIfAbsent(
            record,
            x -> new AtomicInteger(DEFAULT_VALUE)
        );
        counter.set(value);

        recordCurrentVersion.merge(record, 1L, Long::sum);
    }

    public RequestData executeAhead(Entity entity, RequestData request) {
        this.entity = entity;
        List<Integer> values = new ArrayList<>();

        for (var op : request.getWriteSetList()) {
            runComputeDummy(request);
            values.add(processRequest(op));
        }
        for (var op : request.getReadSetList()) {
            values.add(processRequest(op));
        }

        try {
            return request.toBuilder()
                .setEarlyExecResult(values.get(0))
                .setCurrentVersion(
                    recordCurrentVersion.getOrDefault(
                        request.getWriteSetList().isEmpty()
                            ? (request.getReadSetList().isEmpty() ? -1 : request.getReadSetList().getFirst().getRecord())
                            : request.getWriteSetList().getFirst().getRecord(),
                        0L
                    )
                )
                .build();
        } catch (Exception e) {
            // log or rethrow as needed
            return null;
        }
    }

    public List<RequestData> executeRequestsAhead(Entity entity, List<RequestData> block) {
        List<RequestData> result = new ArrayList<>(block.size());
        for (RequestData req : block) {
            result.add(this.executeAhead(entity, req));
        }
        return result;
    }

    public List<RequestData> validateRequests(List<RequestData> block, Map<Long, Integer> replies) {
        Map<Integer, Long> versionMap = new TreeMap<>();
        List<RequestData> validated = new ArrayList<>();

        for (RequestData req : block) {
            boolean isValid = validate(req, replies, versionMap);
            if(req.getWriteSetCount() > 0) {
                entity.addWriteTransactionsCount(1);
                entity.addTotalTransactionsCount(1);
            }
            else{
                entity.addTotalTransactionsCount(1);
            }
            validated.add(req.toBuilder().setIsTnxValid(isValid).build());
        }

        return validated;
    }

    public void writeData(RequestData request) {
        for (var op : request.getWriteSetList()) {
            int record = op.getRecord();
            // computeIfAbsent returns a non-null AtomicInteger
            AtomicInteger counter = records.computeIfAbsent(
                record,
                x -> new AtomicInteger(DEFAULT_VALUE)
            );
            // safely overwrite with the early-exec result
            counter.set(request.getEarlyExecResult());
        }
    }


    private boolean validate(RequestData request, Map<Long, Integer> replies, Map<Integer, Long> versionMap) {
        for (var op : request.getWriteSetList()) {
            int rec = op.getRecord();
            if (versionMap.getOrDefault(rec, 0L) != 0) {
                replies.put(request.getRequestNum(), 0);
                return false;
            }
        }
        for (var op : request.getReadSetList()) {
            int rec = op.getRecord();
            if (versionMap.getOrDefault(rec, 0L) != 0) {
                replies.put(request.getRequestNum(), 0);
                return false;
            }
            replies.put(request.getRequestNum(), DEFAULT_VALUE);
        }
        for (var op : request.getWriteSetList()) {
            int rec = op.getRecord();
            replies.put(request.getRequestNum(), request.getEarlyExecResult());
            AtomicInteger counter = records.computeIfAbsent(
                rec,
                x -> new AtomicInteger(DEFAULT_VALUE)
            );
            counter.set(request.getEarlyExecResult());
            versionMap.put(rec, 1L);
        }
        return true;
    }

    public int processRequestAhead(OperationSet operation) {
        AtomicInteger counter = records.get(operation.getRecord());
        if (counter == null) {
            counter = new AtomicInteger(DEFAULT_VALUE);
        }
        return switch (operation.getOp()) {
            case ADD -> counter.get() + operation.getValue();
            case SUB -> counter.get() - operation.getValue();
            case INC -> counter.get() + 1;
            case DEC -> counter.get() - 1;
            default -> counter.get();
        };
    }

    public Map<Integer, Long> getRecordCurrentVersion() {
        return recordCurrentVersion;
    }

    public void setRecordCurrentVersion(Map<Integer, Long> recordCurrentVersion) {
        this.recordCurrentVersion = recordCurrentVersion;
    }

    public Map<Integer, Long> getRecordLatestVersion() {
        return recordLatestVersion;
    }

    public void setRecordLatestVersion(Map<Integer, Long> recordLatestVersion) {
        this.recordLatestVersion = recordLatestVersion;
    }
}
