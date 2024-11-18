package com.gbft.framework.core;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Dataset {

    protected Map<Integer, AtomicInteger> records;

    @Getter
    protected Map<Integer, Long> recordCurrentVersion;

    @Getter
    public Map<Integer, Long> recordLatestVersion;

    public static final int DEFAULT_VALUE = 1000;
    public static final int RECORD_COUNT = Config.integer("workload.dataset-size");

    public Dataset() {
        records = DataUtils.concurrentMapWithDefaults(RECORD_COUNT, x -> new AtomicInteger(DEFAULT_VALUE));
        recordCurrentVersion = new TreeMap<>();
        recordLatestVersion = new TreeMap<>();
    }

    // use this for copying service state
    public Dataset(Dataset dataset) {
        records = new TreeMap<>();
        for (var entry : dataset.records.entrySet()) {
            this.records.put(entry.getKey(), new AtomicInteger(entry.getValue().get()));
        }
        recordCurrentVersion = new TreeMap<>();
        recordLatestVersion = new TreeMap<>();

    }

    public void setRecords(Map<Integer, Integer> records) {
        this.records.clear();
        for (var entry : records.entrySet()) {
            this.records.put(entry.getKey(), new AtomicInteger(entry.getValue()));
        }
    }
    
    public Map<Integer, AtomicInteger> getRecords() {
        return records;
    }

    public int execute(RequestData request) {
        runComputeDummy(request);

        List<Integer> values = new ArrayList<>();

        for (var op: request.getWriteSetList()){
            runComputeDummy(request);
            values.add( processRequest(op) );
        }

        for (var op: request.getReadSetList()){
            runComputeDummy(request);
            values.add( processRequest(op));
        }

        return values.getFirst();
    }

    public void runComputeDummy(RequestData request){
        // dummy computation
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (request.getComputeFactor() > 0) {
            var dummy_counter = 0;
            var random = new Random();
            for (int i = 0; i < request.getComputeFactor(); i ++) {
                dummy_counter += random.nextInt();
            }
            try {
                OutputStream.nullOutputStream().write(dummy_counter);
            } catch (IOException e) {}
        }
    }


    public int processRequest(OperationSet operation){
        return switch (operation.getOp()) {
            case ADD -> records.get(operation.getRecord()).addAndGet(operation.getValue());
            case SUB -> records.get(operation.getRecord()).addAndGet(-operation.getValue());
            case INC -> records.get(operation.getRecord()).incrementAndGet();
            case DEC -> records.get(operation.getRecord()).decrementAndGet();
            default -> records.get(operation.getRecord()).get();
        };
    }

    public void update(RequestData request, int value) {

        if(request.getWriteSetList().isEmpty()) return;

        var record = request.getWriteSetList().getFirst().getRecord();
        records.get(record).set(value);

        this.recordCurrentVersion.put(record, recordCurrentVersion.getOrDefault(record, 0L) + 1);
    }

    public RequestData executeAhead(RequestData request) {

        List<Integer> values = new ArrayList<>();

        for (var op: request.getWriteSetList()){
            values.add( processRequest(op) );
        }

        for (var op: request.getReadSetList()){
            values.add( processRequest(op));
        }

        return request.toBuilder().setEarlyExecResult(values.getFirst())
                .setCurrentVersion(recordCurrentVersion.getOrDefault(
                        !request.getWriteSetList().isEmpty() ?
                                request.getWriteSetList().getFirst() :
                                (!request.getReadSetList().isEmpty() ? request.getReadSetList().getFirst() : null )
                        , 0L))
                .build();
    }

    public int processRequestAhead(OperationSet operation){
        return switch (operation.getOp()) {
            case ADD -> records.get(operation.getRecord()).get()+ operation.getValue();
            case SUB -> records.get(operation.getRecord()).get()-operation.getValue();
            case INC -> records.get(operation.getRecord()).get()+1;
            case DEC -> records.get(operation.getRecord()).get() - 1;
            default -> records.get(operation.getRecord()).get();
        };
    }

}
