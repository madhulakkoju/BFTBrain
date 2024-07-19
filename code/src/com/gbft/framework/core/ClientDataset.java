package com.gbft.framework.core;

import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestData.Operation;
import com.gbft.framework.utils.AdvanceConfig;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;
import com.gbft.framework.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class ClientDataset extends Dataset {

    private int clientId;
    private Random random;
    private Map<Integer, Long> lookahead;

    public ClientDataset(int clientId) {
        super();

        this.clientId = clientId;

        random = new Random();
        lookahead = new HashMap<>();
        IntStream.range(0, RECORD_COUNT)
                 .forEach(record -> lookahead.computeIfAbsent(record, x -> (long) DEFAULT_VALUE));
    }

    @Override
    public void update(RequestData request, int value) {
        super.update(request, value);

        var sender = request.getSender();
        var receiver = request.getReceiver();
        var op = request.getOperation();

        lookahead.put(sender, records.get(sender).longValue());
        lookahead.put(receiver, records.get(receiver).longValue());

//
//        switch (op) {
//            case TRANSACT:
//                lookahead.put(sender, records.get(sender).longValue());
//                lookahead.put(receiver, records.get(receiver).longValue());
//                break;
//            case BONUS:
//                lookahead.put(sender, records.get(sender).longValue());
//                lookahead.put(receiver, records.get(receiver).longValue());
//                break;
//            default:
//                break;
//        }
    }


    public RequestData createRequest(long reqnum) {
        //generate record and operation randomly
        var record = random.nextInt(AdvanceConfig.integer("workload.contention-level"));
        var operation = Operation.values()[random.nextInt(3)];

        var sender = random.nextInt(AdvanceConfig.integer("workload.contention-level"));
        var receiver = random.nextInt(AdvanceConfig.integer("workload.contention-level"));

        while(sender == receiver){
            receiver = random.nextInt(AdvanceConfig.integer("workload.contention-level"));
        }

        if(records.get(sender).intValue() <= 0){
            operation = Operation.BONUS;
        }

        LogUtils.LogCommon("sender: " + sender + ", receiver: " + receiver + ", operation: " + operation.name());

        int value = 0;

        switch (operation) {
            case TRANSACT:
                value = records.get(sender).intValue() > 0 ? random.nextInt( records.get(sender).intValue() ) : 0;
                break;
            case BONUS:
                value = DEFAULT_VALUE;
                break;
            default:
                break;

        }

        // generate read only optimization
        if (Config.stringList("plugins.message").contains("read-only")) {
            if (random.nextDouble() < AdvanceConfig.doubleNumber("workload.read-only-ratio")) {
                operation = Operation.READ_ONLY;
            }
        }

        return DataUtils.createRequest(reqnum, sender, receiver, operation, value, clientId);
    }

}
