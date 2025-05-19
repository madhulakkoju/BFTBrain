package com.gbft.framework.core;

import com.gbft.framework.coordination.CoordinatorUnit;
import com.gbft.framework.data.MessageData;
import com.gbft.framework.data.Operation;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.statemachine.StateMachine;
import com.gbft.framework.statemachine.Transition.UpdateMode;
import com.gbft.framework.utils.*;
import com.gbft.framework.utils.MessageTally.QuorumId;
import com.gbft.framework.utils.Printer.Verbosity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;


public class Client extends Entity {

    protected long nextRequestNum;
    protected long intervalns;
    protected final int requestTargetRole;

    protected ClientDataset dataset;

 
    public Client(int id, CoordinatorUnit coordinator) {
        super(id, coordinator);
        intervalns = Config.integer("benchmark.request-interval-micros") * 1000L;
        var targetConfig = Config.string("protocol.general.request-target");
        requestTargetRole = StateMachine.roles.indexOf(targetConfig);

        dataset = new ClientDataset(id,this);
        nextRequestNum = 0L;

        requestGenerator = createRequestGenerator();
        requestGenerator.init();
    }

    protected RequestGenerator createRequestGenerator() {
        if (Config.bool("benchmark.closed-loop.enable")) {
            return new ClosedLoopRequestGenerator(this);
        } else {
            return new RequestGenerator(this);
        }
    }

    @Override
    protected boolean checkMessageTally(long seqnum, QuorumId quorumId, UpdateMode updateMode) {
        var checkpoint = checkpointManager.getCheckpointForSeq(seqnum);

        var tally = checkpoint.getMessageTally();
        var viewnum = tally.getMaxQuorum(seqnum, quorumId);
        if (viewnum != null && viewnum >= currentViewNum) {
            var block = tally.getQuorumBlock(seqnum, viewnum);
            if (block != null) {
                registerBlock(seqnum, block);
            }
            return true;
        }

        return false;
    }

    @Override
    protected void execute(long seqnum) {
        var checkpoint = checkpointManager.getCheckpointForSeq(seqnum);

        var tally = checkpoint.getMessageTally();
        var viewnum = tally.getMaxQuorum(seqnum);
        var replies = tally.getQuorumReplies(seqnum, viewnum);
        //logger.write("seqnum execute "+seqnum +" "+replies);
        currentViewNum = viewnum;

        if (replies != null) {
            checkpoint.addReplies(seqnum, replies);
            var now = System.nanoTime();
            for (var entry : replies.entrySet()) {
                var reqnum = entry.getKey();
                var request = checkpoint.getRequest(reqnum);
                if(entry.getValue() == 0)
                    continue;
                dataset.update(request, entry.getValue());

                // benchmarkManager.requestExecuted(reqnum, now);
            }

            requestGenerator.execute();

        }
    }

    @Override
    public Map<String, String> reportBenchmark() { 
        var benchmark = benchmarkManager.getBenchmarkById(reportnum);

        var report = new HashMap<String, String>();
        var executeMax = benchmark.max(BenchmarkManager.REQUEST_EXECUTE);
        var executeAvg = benchmark.average(BenchmarkManager.REQUEST_EXECUTE);
        var executeCount = benchmark.count(BenchmarkManager.REQUEST_EXECUTE);
        report.put("request-execute",
                "avg: " + Printer.timeFormat(executeAvg, true) + ", max: " + Printer.timeFormat(executeMax, true) + ", count: "
                        + executeCount);
        report.put("request-interval", Printer.timeFormat(intervalns, true));

        var interval = Config.integer("benchmark.benchmark-interval-ms");
        var throughput = executeCount / (interval / 1000.0);
        report.put("throughput", String.format("%.2freq/s", throughput));
        report.put("last-executed-sequence", "num: " + lastExecutedSequenceNum);
        report.put("current-episode", "value: " + currentEpisodeNum.get());
        report.put("current-protocol", "value: " + checkpointManager.getCheckpoint(currentEpisodeNum.get()).getProtocol());
//        report.put("current-architecture", "value: " +
//                checkpointManager.getCheckpoint(currentEpisodeNum.get()).getArchitecture() == null ? "XOV" :
//                checkpointManager.getCheckpoint(currentEpisodeNum.get()).getArchitecture());
        report.put("current-architecture", "value: " + checkpointManager.getCheckpoint(currentEpisodeNum.get()).getArchitecture());
        var blockCount = benchmark.count(BenchmarkManager.BLOCK_EXECUTE);
        var timeoutCount = benchmark.count(BenchmarkManager.TIMEOUT);
        report.put("slow-path", String.format("ratio: %.2f",  (double) timeoutCount / (double) blockCount));

        report.put("total-committed-transactions", String.valueOf(this.totalCommittedTransactions));
        String benchmarkLogString = ""+
                    checkpointManager.getCheckpoint(currentEpisodeNum.get()).getProtocol()+","+
                    checkpointManager.getCheckpoint(currentEpisodeNum.get()).getArchitecture() + ","+
                    AdvanceConfig.integer("workload.contention-level") + "," +
                    AdvanceConfig.integer("benchmark.block-size")  + "," +
                    String.format("%.2f", throughput);

        CustomBenchmarks.LogTotalCommittedTnxs(this.totalCommittedTransactions);
        logger.write("[Client]: "+ benchmarkLogString);

        reportnum += 1;
        return report;
    }

    @Override
    public boolean isClient() {
        return true;
    }



    public class RequestGenerator {
        public Client client;

        public RequestGenerator() {
        }

        public RequestGenerator(Client client) {
            this.client = client;
        }

        public void init() {
            threads.add(new Thread(new RequestGeneratorRunner()));
        }

        public void init(Client client) {
            this.init();
            this.client = client;
        }


        protected class RequestGeneratorRunner implements Runnable {

            public Client client;
            @Override
            public void run() {

                while (running) {
                    var next = System.nanoTime() + intervalns;

                    var request = dataset.createRequest(nextRequestNum);
                    nextRequestNum += 1;
                    // from here sending generated request to leader
//                    String curr_arch = "";
//                    if(request != null){
//                        curr_arch = request.getCurrArchitecture();
//                    }
//                    if(curr_arch.contains("XOV")) {
//                        sendEndorserRequest(request);
//                    }
//                    else{
//                        sendRequest(request);
//                    }
                    sendRequest(request);
                    while (System.nanoTime() < next) {
                        LockSupport.parkNanos(intervalns / 3);
                    }
                }
            }
        }

        protected void sendRequest(RequestData request) {
            var reqnum = request.getRequestNum();
            var seqnum = reqnum / blockSize;
            var view = currentViewNum;
            // wait to know the leader mode if necessary
            var episode = getEpisodeNum(seqnum);
            rolePlugin.roleReadLock.lock();
            try {
                if (rolePlugin.episodeLeaderMode.get(episode) == null) {
                    rolePlugin.roleReadLock.unlock();
                    rolePlugin.roleWriteLock.lock();
                    try {
                        while (rolePlugin.episodeLeaderMode.get(episode) == null) {
                            rolePlugin.roleCondition.await();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        rolePlugin.roleWriteLock.unlock();
                        rolePlugin.roleReadLock.lock();
                    }
                }
            } finally {
                rolePlugin.roleReadLock.unlock();
            }

            var targets = rolePlugin.getRoleEntities(seqnum, view, StateMachine.NORMAL_PHASE, requestTargetRole);

            if (RequestUtils.getOperation(request).getNumber()  == Operation.READ_ONLY_VALUE) {
                targets = rolePlugin.getRoleEntities(seqnum, view, StateMachine.NORMAL_PHASE, StateMachine.NODE);
            }


            var message = createMessage(null, view, List.of(request), StateMachine.REQUEST, id, targets);
            sendMessage(message);
            if (Printer.verbosity >= Verbosity.VVV) {
                Printer.print(Verbosity.VVV, prefix, "Request created: ", request);
            }
        }



        //Endorsement Requests

        public MessageData createEndorsementMessage(Long seqnum, long viewNum, List<RequestData> block, int type, int source,
                                         List<Integer> targets) {
            var message = createMessage(seqnum, viewNum, block, type, source, targets);
            message = message.toBuilder().setIsEndorsementRequest(true).setXovState(1).build();

            return processMessage(message);
        }


        protected void sendEndorserRequest(RequestData request) {
            try{
                var reqnum = request.getRequestNum();
                var seqnum = reqnum / blockSize;
                var view = currentViewNum;

                // wait to know the leader mode if necessaryf
                var episode = getEpisodeNum(seqnum);
                rolePlugin.roleReadLock.lock();
                try {
                    if (rolePlugin.episodeLeaderMode.get(episode) == null) {
                        rolePlugin.roleReadLock.unlock();
                        rolePlugin.roleWriteLock.lock();
                        try {
                            while (rolePlugin.episodeLeaderMode.get(episode) == null) {
                                rolePlugin.roleCondition.await();
                            }
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            rolePlugin.roleWriteLock.unlock();
                            rolePlugin.roleReadLock.lock();
                        }
                    }
                } finally {
                    rolePlugin.roleReadLock.unlock();
                }

                var nodesTargetRole = StateMachine.roles.indexOf("nodes");

                var targetsList = rolePlugin.getRoleEntities(seqnum, view, StateMachine.NORMAL_PHASE, nodesTargetRole);

                List<Integer> targets = new ArrayList<>();
               // LogUtils.LogCommon("SEND ENDORSER REQ " + targetsList.toString());
                int noOfEndorsers = targetsList.size();
                targets.add((int)reqnum % noOfEndorsers);
//                targets.add(0);
//                targets.add(1);
//                targets.add(2);
//                targets.add(3);

//                for(var target : targetsList) {
//                    if(target %2 == 0 && reqnum %2 == 0) {
//                        targets.add(target);
//                    }
//                    if(target %2 == 1 && reqnum %2 == 1) {
//                        targets.add(target);
//                    }
//                }

//                LogUtils.LogCommon("SEND ENDORSER REQ TARGETSLIST " + targetsList.toString());
//                LogUtils.LogCommon("SEND ENDORSER REQ TARGETS " + targets.toString());


                if ( RequestUtils.getOperation(request).getNumber() == Operation.READ_ONLY_VALUE) {
                    targets = rolePlugin.getRoleEntities(seqnum, view, StateMachine.NORMAL_PHASE, StateMachine.NODE);
                  //  LogUtils.LogCommon("TARGETS CHANGED with roleplugin because Operation READ_ONLY " + targets.toString());
                }

              //  LogUtils.LogCommon("SEND ENDORSER REQ TARGETS " + targets.toString());

                var message = createEndorsementMessage(null, view, List.of(request), StateMachine.REQUEST, id, targets);

//                for (var req : message.getRequestsList()) {
//                    this.client.getEndorsementQueue().put(req.getRequestNum(), message);
//                    this.client.getEndorsementCounts().put(req.getRequestNum(), 0);
//                }
                
                sendMessage(message);

                if (Printer.verbosity >= Verbosity.VVV) {
                    Printer.print(Verbosity.VVV, prefix, "Endorser Request created: ", request);
                }



                //this.client.getEndorsementQueue().put(message.getRequestsList().get(0).getRequestNum(), message);


            }catch (Exception e){
                System.out.println("[SendEndorserRequest] Exception in client 320 "+e);
                System.exit(1);
            }

        }


        protected void execute() {}
    }


    public class ClosedLoopRequestGenerator extends RequestGenerator {
        protected final Semaphore semaphore = new Semaphore(Config.integer("benchmark.closed-loop.num-client"));
        protected final int block_size = Config.integer("benchmark.block-size");

        protected AtomicLong nextRequestNum = new AtomicLong(0l);

        public ClosedLoopRequestGenerator(Client client) {
            super(client);
        }

        public ClosedLoopRequestGenerator(AtomicLong nextRequestNum, long reqnumcnt) {
            this.nextRequestNum = nextRequestNum;
            this.reqnumcnt = reqnumcnt;
        }

        public ClosedLoopRequestGenerator() {
            super();
        }

        protected long reqnumcnt = 0l;

        @Override
        public void init() {
            for (int i = 0; i < Config.integer("benchmark.closed-loop.num-client"); i ++) {
                threads.add(new Thread(new ClosedLoopRequestGeneratorRunner(client)));
            }
        }

        protected class ClosedLoopRequestGeneratorRunner implements Runnable {

            public ClosedLoopRequestGeneratorRunner(Client client) {
                this.client = client;
            }

            public ClosedLoopRequestGeneratorRunner() {
            }

            public Client client;

            @Override
            public void run() {
                while (running) {
                    try {
                        semaphore.acquire();

                        // sleep for `delay` ms
                        var delay = AdvanceConfig.integer("benchmark.closed-loop.delay-ms");
                        Thread.sleep(delay);

                        var read_only_buf = 0;

                        for (int i = 0; i < block_size + read_only_buf; i ++) {
                            var reqnum = nextRequestNum.getAndIncrement();
                            var request = dataset.createRequest(reqnum);

                            if (  RequestUtils.getOperation(request).getNumber() == Operation.READ_ONLY_VALUE) {
                                read_only_buf ++;
                            }

                            // System.out.println("client " + id + " record " + (++ reqnumcnt));
                            // System.out.println("client " + id + " send request " + reqnum);
                            String curr_arch = "";
                            if(request != null){
                                curr_arch = request.getCurrArchitecture();
                            }
                            if( this.client != null &&
                                this.client.getArchManager() != null && curr_arch.contains("XOV")) {
                                sendEndorserRequest(request);
                            }
                            else {
                                sendRequest(request);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        @Override
        protected void execute() {
            // System.out.println("client " + id + " execute");
            semaphore.release();
        }
    }





}
