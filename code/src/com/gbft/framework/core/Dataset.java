package com.gbft.framework.core;

import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;
import com.gbft.framework.utils.LogUtils;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
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

        RequestData.Operation op = request.getOperation();

        var sender = request.getSender();
        var receiver = request.getReceiver();

        // dummy computation
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

        int value = 0;

        switch (op) {
            case TRANSACT:
                value = records.get(receiver).addAndGet(request.getValue());
                records.get(sender).addAndGet(-request.getValue());
                break;
            case BONUS:
                value = records.get(sender).addAndGet(request.getValue());
                records.get(receiver).addAndGet(request.getValue());
                break;
            case READ_ONLY:
                value = records.get(sender).get();
            default:
                value = records.get(sender).get();
        }

        return value;
    }

    public void update(RequestData request, int value) {
        var sender = request.getSender();
        var receiver = request.getReceiver();
        records.get(sender).addAndGet(-value);
        records.get(receiver).addAndGet(value);

        this.recordCurrentVersion.put(sender, recordCurrentVersion.getOrDefault(sender, 0L) + 1);
        this.recordCurrentVersion.put(receiver, recordCurrentVersion.getOrDefault(receiver, 0L) + 1);

        LogUtils.LogCommon("Update Sender: " + sender + " , Receiver: " + receiver
                + "versions: sender - "+recordCurrentVersion.get(sender) + " receiver: " +
                recordCurrentVersion.get(receiver));
    }

    public RequestData executeAhead(RequestData request) {
        var op = request.getOperation();
        var sender = request.getSender();
        var receiver = request.getReceiver();

        int value = 0;

        switch (op) {
            case TRANSACT:
                value = records.get(sender).get() - request.getValue();
                break;
            case BONUS:
                value = records.get(sender).get() + request.getValue();
                break;
            default:
                value = records.get(sender).get();
        }
        return request.toBuilder().setEarlyExecResult(value)
                .setCurrentVersion(recordCurrentVersion.getOrDefault(sender, 0L))
                .build();
    }


}
