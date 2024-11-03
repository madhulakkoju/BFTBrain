package com.gbft.framework.core;

import com.gbft.framework.coordination.CoordinatorUnit;
import com.gbft.framework.data.LearningData;
import com.gbft.framework.data.MessageData;
import com.gbft.framework.data.RequestDataList;
import com.gbft.framework.fault.PollutionFault;
import com.gbft.framework.statemachine.StateMachine;
import com.gbft.framework.utils.AdvanceConfig;
import com.gbft.framework.utils.DataUtils;
import com.gbft.framework.utils.FeatureManager;
import com.gbft.plugin.message.CheckpointMessagePlugin;
import com.gbft.plugin.message.LearningMessagePlugin;

import java.beans.Expression;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;






public class Node extends Entity {





    public Node(int id, CoordinatorUnit coordinator) {
        super(id, coordinator);
    }

//    public void executeParallel(ConcurrentHashMap<Long, Integer> replies,List<RequestDataList> dependencyGraph) {
//        // Define the number of threads you want to use
////        ExecutorService executor = Executors.newFixedThreadPool(dependencyGraph.size());
//
//            try {
//                // Submit each task to the executor for parallel execution
//                List<Thread> threads = new ArrayList<>();
//                for (RequestDataList requestDataList : dependencyGraph) {
////                    executor.submit(() -> this.executeTransaction(replies, requestDataList));
//                    executeTransaction(replies, requestDataList);
////                    Thread thread = new Thread(() -> this.executeTransaction(replies, requestDataList));
////                    thread.start();
////                    threads.add(thread);
//                }
////                for (Thread thread : threads) {
////                    try {
////                        thread.join(); // Join each thread to ensure it completes
////                    } catch (InterruptedException e) {
////                        e.printStackTrace(); // Handle interruption
////                    }
////                }
//
//            }
//            catch (Exception e){
//                logger.write("Exception "+ e);
//            }
////            finally {
////                // Gracefully shut down the executor
////                //executor.shutdown();
////            }
//    }

    public void executeParallel(ConcurrentHashMap<Long, Integer> replies, List<RequestDataList> dependencyGraph) {
        ExecutorService executor = Executors.newFixedThreadPool(dependencyGraph.size());

        try {
            for (RequestDataList requestDataList : dependencyGraph) {
                executor.submit(() -> this.executeTransaction(replies, requestDataList));
            }
        } catch (Exception e) {
            logger.write("Exception " + e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    public void executeTransaction(ConcurrentHashMap<Long, Integer> replies, RequestDataList requestDataList) {
        var requestData = requestDataList.getReqDataListList();
        for (var request : requestData) {
            replies.put(request.getRequestNum(), dataset.execute(request));
        }
        // Log the collected record string for this transaction
    }


    // TODO: Update this to use Architecture based Execution
    @Override
    protected void execute(long seqnum) {
        logger.write("execute seqnum "+seqnum);
        var checkpoint = checkpointManager.getCheckpointForSeq(seqnum);
        var requestBlock = checkpoint.getRequestBlock(seqnum);

        if (checkpoint.getReplies(seqnum) == null && !this.getArchManager().getCurrentArchitectureKey().contains("OX")) {
            var replies = new HashMap<Long, Integer>();
            for (var request : requestBlock) {

//                if(this.getArchManager().getCurrentArchitectureKey().contains("XOV")){
//                    if(this.getArchManager().getCurrentArchitecture().isValidRequest(request)){
//
//                        //TODO: something seems odd in this type of validation and updation
//                        logger.write("Node:: Request is validated amd updated with "+ request.getEarlyExecResult());
//                        replies.put(request.getRequestNum(), request.getEarlyExecResult());
//                        logger.write("Node:: Dataset is updated with value "+ request.getEarlyExecResult());
//                        //update value on node dataset
//                        dataset.update(request, request.getEarlyExecResult());
//                    }
//                }
//                else{
//                      replies.put(request.getRequestNum(), dataset.execute(request));
//                }

                replies.put(request.getRequestNum(), dataset.execute(request));

            }
            if(checkpoint.getRequestBlock(seqnum) == null){
                logger.write("block is null");
                logger.write("replies "+replies);
            }

            checkpoint.addReplies(seqnum, replies);
        }

        else if (checkpoint.getReplies(seqnum) == null && this.getArchManager().getCurrentArchitectureKey().contains("OX")) { // make true for oxii
            var replies = new ConcurrentHashMap<Long, Integer>();
            List<RequestDataList> dependencyGraph = checkpoint.getDependencyGraph(seqnum);
            if(checkpoint.getDependencyGraph(seqnum) == null) {
                if(checkpoint.getRequestBlock(seqnum) == null){
                    logger.write("block is null");
                    for (var request : requestBlock) {
                        replies.put(request.getRequestNum(), dataset.execute(request));
                    }
                }
                else{
                    logger.write("null block " + checkpoint.getRequestBlock(seqnum));
                }

            }else{
                logger.write("node dag"+checkpoint.getDependencyGraph(seqnum).size() +" seq num "+ seqnum + " protocol "+ checkpoint.getProtocol());
                executeParallel(replies,dependencyGraph);
            }


//            logger.write("replies"+ replies);
            checkpoint.addReplies(seqnum, replies);
        }

        // checkpoint
        if ((seqnum + 1) % checkpointSize == 0) {
            // copy the current state to checkpoint, new requests can commit but not execute.
            checkpoint.setServiceState(dataset);

            // update h
            for (var i = messagePlugins.size() - 1; i >= 0; i--) {
                var plugin = messagePlugins.get(i);
                if (plugin instanceof CheckpointMessagePlugin) {
                    CheckpointMessagePlugin checkpointPlugin = (CheckpointMessagePlugin) plugin;
                    if (checkpointPlugin.hasQuorum(seqnum / checkpointSize)) {
                        checkpointManager.setLowWaterMark(seqnum / checkpointSize);
                    }
                }
            }

            // multicast CHECKPOINT message to all other nodes
            new Thread(() -> checkpointManager.sendCheckpoint(seqnum / checkpointSize)).start();
        }

        // report local features and reward
        if (learning) {
            if (seqnum <= reportSequence) {
                // request size 
                var request_size = AdvanceConfig.integer("workload.payload.request-size");
                for (var request : requestBlock) {
                    featureManager.add(currentEpisodeNum.get(), FeatureManager.REQUEST_SIZE, request_size);
                    // featureManager.add(currentEpisodeNum.get(), FeatureManager.REQUEST_SIZE, request.getRequestDummy().size());
                }
                // fast path frequency
                featureManager.count(currentEpisodeNum.get(), FeatureManager.FAST_PATH_FREQUENCY);
                // slow proposal is tracked upon first receiving that message
            }

            if (seqnum == reportSequence) {
                // if in dark there will be no report by default
                var targets = getRolePlugin().getRoleEntities(seqnum, 0, StateMachine.NORMAL_PHASE, StateMachine.NODE);
                // seqnum represents episode number
                var message = DataUtils.createMessage((long)currentEpisodeNum.get(), 0L, REPORT, getId(), targets, List.of(), EMPTY_BLOCK,
                        null, EMPTY_DIGEST);
                var extractor = featureManager.getExtractor(currentEpisodeNum.get());

                Map<Integer, Float> report = new HashMap<>();
                if (currentEpisodeNum.get() != 0) {
                    var prev_checkpoint = checkpointManager.getPrevCheckpointForSeq(seqnum);
                    if (pollutionFault.getType(this.id) == PollutionFault.POLLUTION_SBFT && prev_checkpoint.getProtocol().equals("sbft")) {
                        report.put(FeatureManager.REWARD, prev_checkpoint.throughput * 2.5f);
                    } else if (pollutionFault.getType(this.id) == PollutionFault.POLLUTION_ALL) {
                        report.put(FeatureManager.REWARD, PollutionFault.randomFeatureGenerator(50000));
                    } else {
                        report.put(FeatureManager.REWARD, prev_checkpoint.throughput);
                    }
                }

                if (pollutionFault.getType(this.id) == PollutionFault.POLLUTION_ALL) {
                    report.put(FeatureManager.REQUEST_SIZE, PollutionFault.randomFeatureGenerator(500000f));
                    report.put(FeatureManager.FAST_PATH_FREQUENCY, PollutionFault.randomFeatureGenerator(1f));
                    report.put(FeatureManager.SLOWNESS_OF_PROPOSAL, PollutionFault.randomFeatureGenerator(100f));
                    report.put(FeatureManager.RECEIVED_MESSAGE_PER_SLOT, (float) Math.round(PollutionFault.randomFeatureGenerator(100f)));
                    report.put(FeatureManager.HAS_FAST_PATH, PollutionFault.randomOnehot());
                    report.put(FeatureManager.HAS_LEADER_ROTATION, PollutionFault.randomOnehot());
                } else {
                    // request
                    report.put(FeatureManager.REQUEST_SIZE, (float) extractor.average(FeatureManager.REQUEST_SIZE));
                    // fast path frequency
                    if (featureManager.hasFastPath.get(checkpoint.getProtocol()) == 0) {
                        report.put(FeatureManager.FAST_PATH_FREQUENCY, (float) 0.0);
                    } else {
                        report.put(FeatureManager.FAST_PATH_FREQUENCY, 
                                1 - (float) extractor.getRatio(FeatureManager.FAST_PATH_FREQUENCY, FeatureManager.SLOW));
                    }
                    // slow proposal
                    report.put(FeatureManager.SLOWNESS_OF_PROPOSAL, extractor.getProposalSlowness());
                    // number of received messages per slot
                    report.put(FeatureManager.RECEIVED_MESSAGE_PER_SLOT, (float) extractor.count(FeatureManager.RECEIVED_MESSAGE_PER_SLOT));
                    // protocol encodings
                    report.put(FeatureManager.HAS_FAST_PATH, (float) featureManager.hasFastPath.get(checkpoint.getProtocol()));
                    report.put(FeatureManager.HAS_LEADER_ROTATION, (float) featureManager.hasLeaderRotation.get(checkpoint.getProtocol()));
                }

                var learningDataBuilder = LearningData.newBuilder().putAllReport(report);
                message = message.toBuilder().setReport(learningDataBuilder).build();

                sendMessage(message);
                if (message.getTargetsList().contains(id)) {
                    // deliver to myself
                    for (var i = 0; i < messagePlugins.size(); i++) {
                        var plugin = messagePlugins.get(i);
                        if (plugin instanceof LearningMessagePlugin) {
                            final MessageData _message = message;
                            // new Thread( () -> plugin.processIncomingMessage(_message)).start();
                            plugin.processIncomingMessage(_message);
                            break;
                        }
                    }

                }
                System.out.println("sending report for episode " + currentEpisodeNum.get() + ", reportSequence=" + reportSequence);
            }

            if (seqnum == exchangeSequence) {
                Map<Integer, List<Float>> featureToQuorum = new HashMap<>();
                for (var report : requestBlock.get(0).getReportQuorumList()) {
                    for (var entry : report.getReportMap().entrySet()) {
                        featureToQuorum.computeIfAbsent(entry.getKey(), f ->  new ArrayList<>()).add(entry.getValue());
                    }
                }

                // take median and send to learning agent
                Map<Integer, Float> featureToMedian = featureToQuorum.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> calculateMedian(entry.getValue())));
                // reuse the proto field `next_protocol` to store the current protocol just for convenience
                var learningData = LearningData.newBuilder().putAllReport(featureToMedian).setNextProtocol(checkpoint.getProtocol()).build();
                new Thread(() -> agentStub.sendData(learningData)).start(); 
                System.out.println("notify learning agent for episode " + currentEpisodeNum.get() + ", exchangeSequence=" + exchangeSequence);
            }
        }
    }

    @Override
    public boolean isClient() {
        return false;
    }

    private float calculateMedian(List<Float> values) {
        List<Float> sortedValues = values.stream().sorted().collect(Collectors.toList());

        int size = sortedValues.size();
        if (size % 2 == 0) {
            int midIndex = size / 2;
            return (sortedValues.get(midIndex - 1) + sortedValues.get(midIndex)) / 2.0f;
        } else {
            int midIndex = size / 2;
            return sortedValues.get(midIndex);
        }
    }
}


//class OXPPWorkerThread extends Thread{
//    public RequestDataList requestDataList;
//    public Node node;
//    public HashMap<Long, Integer> replies;
//
//    public OXPPWorkerThread( RequestDataList requestDataList, HashMap<Long, Integer> replies, Node node ){
//        this.requestDataList = requestDataList;
//        this.node = node;
//        this.replies = replies;
//    }
//
//    @Override
//    public void run(){
//        node.executeTransaction(replies, requestDataList);
//    }
//
//}


