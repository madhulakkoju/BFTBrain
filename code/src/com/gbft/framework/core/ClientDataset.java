package com.gbft.framework.core;

import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.Operation;
import com.gbft.framework.utils.AdvanceConfig;
import com.gbft.framework.utils.Config;
import com.gbft.framework.utils.DataUtils;
import com.gbft.framework.utils.RequestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

public class ClientDataset extends Dataset {

    private int clientId;
    private Random random;
    private Map<Integer, LongAdder> lookahead;
    public Client client;

    public ClientDataset(int clientId, Client client) {
        super();

        this.clientId = clientId;
        this.client = client;

        random = new Random();
        lookahead = new HashMap<>();
        IntStream.range(0, RECORD_COUNT)
                 .forEach(record -> lookahead.computeIfAbsent(record, x -> new LongAdder()).add(DEFAULT_VALUE));
    }

    @Override
    public void update(RequestData request, int value) {
        if(request==null) return;

        super.update(request, value);

        var record = RequestUtils.getRecord(request);

        var op = RequestUtils.getOperation(request);


        switch (op) {
        case INC:
            lookahead.get(record).increment();
            break;
        case ADD:
            lookahead.get(record).add(request.getValue());
            break;
        default:
            break;
        }
    }

    public RequestData createRequest(long reqnum) {
        //generate record and operation randomly
        try {
            String curr_architecture = this.client.getArchManager().getCurrentArchitectureKey();
            var record = random.nextInt(AdvanceConfig.integer("workload.contention-level"));
            var operation = Operation.values()[random.nextInt(5)];
            int value = 0;

            switch (operation) {
                case ADD:
                    value = random.nextInt(DEFAULT_VALUE);
                    break;
                case SUB:
                    var max = Math.min(DEFAULT_VALUE, lookahead.get(record).intValue());
                    if (max <= 0) { // < 0 to fix #47
                        operation = Operation.NOP;
                    } else {
                        value = random.nextInt(max);
                        lookahead.get(record).add(-value);
                    }
                    break;
                case DEC:
                    if (lookahead.get(record).intValue() < 1) {
                        operation = Operation.NOP;
                    } else {
                        lookahead.get(record).decrement();
                    }
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
            return DataUtils.createRequest(reqnum, record, operation, value, clientId, random.nextInt(3), curr_architecture);
        }catch (Exception e){
            System.out.println("Client dataset 100 "+e);
            System.exit(1);
            return null;
        }
    }

    public RequestData createRequest(long reqnum, String curr_architecture) {
        //generate record and operation randomly
        try {
            var record = random.nextInt(AdvanceConfig.integer("workload.contention-level"));
            var operation = Operation.values()[random.nextInt(5)];
            int value = 0;

            switch (operation) {
                case ADD:
                    value = random.nextInt(DEFAULT_VALUE);
                    break;
                case SUB:
                    var max = Math.min(DEFAULT_VALUE, lookahead.get(record).intValue());
                    if (max <= 0) { // < 0 to fix #47
                        operation = Operation.NOP;
                    } else {
                        value = random.nextInt(max);
                        lookahead.get(record).add(-value);
                    }
                    break;
                case DEC:
                    if (lookahead.get(record).intValue() < 1) {
                        operation = Operation.NOP;
                    } else {
                        lookahead.get(record).decrement();
                    }
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
            return DataUtils.createRequest(reqnum, record, operation, value, clientId, random.nextInt(3), curr_architecture);
        }catch (Exception e){
            System.out.println("Client dataset 100 "+e);
            System.exit(1);
            return null;
        }
    }

}
