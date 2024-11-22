package com.gbft.framework.core;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;


import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Dataset {

    protected Map<Integer, AtomicInteger> records;
    protected Map<Integer, Long> recordCurrVersion;

    public Map<Integer, Long> recordLatVersion;

    protected Map<Integer, Long> recordCurrentVersion;

    public Map<Integer, Long> recordLatestVersion;

    public Entity entity;

    public static final int DEFAULT_VALUE = 1000;
    public static final int RECORD_COUNT = Config.integer("workload.dataset-size");

    public Dataset() {
        records = DataUtils.concurrentMapWithDefaults(RECORD_COUNT, x -> new AtomicInteger(DEFAULT_VALUE));
        recordCurrentVersion = new TreeMap<>();
        recordLatestVersion = new TreeMap<>();
        recordCurrVersion = new TreeMap<>();
        recordLatVersion = new TreeMap<>();
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
        //runComputeDummy(request);

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

    public RequestData executeAhead(Entity entity,RequestData request) {
        this.entity = entity;
        List<Integer> values = new ArrayList<>();
//        HashSet<Integer> tempRecords = new HashSet<>();

        for (var op: request.getWriteSetList()){
            runComputeDummy(request);
            values.add( processRequest(op) );
            long latestVersion = this.recordLatVersion.getOrDefault(op.getRecord(),Long.valueOf(0));
            this.recordCurrVersion.put(op.getRecord(),latestVersion);
        }

        for (var op: request.getReadSetList()){
            runComputeDummy(request);
            values.add( processRequest(op));
        }

        RequestData rr = null;
        try {

//            entity.logger.write( recordCurrentVersion.getOrDefault(
//                    !request.getWriteSetList().isEmpty() ?
//                            request.getWriteSetList().getFirst().getRecord() :
//                                (!request.getReadSetList().isEmpty() ?
//                                        request.getReadSetList().getFirst().getRecord() :
//                                        -1 )
//                    , 0L).toString() );


            rr = request.toBuilder().setEarlyExecResult(values.getFirst())
                    .setCurrentVersion(
                            recordCurrentVersion.getOrDefault(
                                    !request.getWriteSetList().isEmpty() ?
                                            request.getWriteSetList().getFirst().getRecord() :
                                            (!request.getReadSetList().isEmpty() ?
                                                    request.getReadSetList().getFirst().getRecord() :
                                                    -1 )
                                    , 0L)
                    )
                    .build();

            return  rr;
        }
        catch (Exception e){
            entity.logger.write("dataset 160 "+e.getMessage());
        }
        return null;
    }

    public List<RequestData> executeRequestsAhead(Entity entity,List<RequestData> block){
        List<RequestData> executeAheadBlock = new ArrayList<>(block.size());

        for (RequestData request : block) {
            executeAheadBlock.add(this.executeAhead(entity,request));
        }
        return executeAheadBlock;
    }

    public List<RequestData> validateRequests(List<RequestData> block,HashMap<Long, Integer> replies){
        Map<Integer, Long> recordVersion= new TreeMap<>();
        List<RequestData> newblock = new ArrayList<>();
        for (RequestData request : block) {
          boolean val = validate(request,replies,recordVersion);
          request = request.toBuilder().setIsTnxValid(val).build();
          newblock.add(request);

        }
        return newblock;
    }

    public boolean validate(RequestData request,HashMap<Long, Integer> replies,Map<Integer, Long> recordVersion){
        for (var op: request.getWriteSetList()){
            int record = op.getRecord();
            //this.entity.logger.write("check record "+ record +"  "+this.recordCurrVersion.getOrDefault(record,Long.valueOf(0))+" "+this.recordLatVersion.getOrDefault(record,Long.valueOf(0)));
//            if(this.recordCurrVersion.getOrDefault(record,Long.valueOf(0)) != this.recordLatVersion.getOrDefault(record,Long.valueOf(0))){
//                replies.put(request.getRequestNum(),0);
//                return false;
//            }
            if(recordVersion.getOrDefault(record,Long.valueOf(0)) != 0){
                replies.put(request.getRequestNum(),0);
                return false;
            }
        }

        for(var op: request.getReadSetList()){
            int record = op.getRecord();
            replies.put(request.getRequestNum(),1000);
        }

        for (var op: request.getWriteSetList()){
            int record = op.getRecord();
            replies.put(request.getRequestNum(),request.getEarlyExecResult());
            records.get(record).set(request.getEarlyExecResult());
            long currVersion = this.recordCurrVersion.getOrDefault(record,Long.valueOf(0))+1;
            this.recordLatVersion.put(record,currVersion+1);
            recordVersion.put(record,Long.valueOf(1));
        }

       return true;
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
