package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.MessageData;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.statemachine.StateMachine;
import com.gbft.framework.utils.MiscUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Architecture {
    protected Entity entity;

    public static int EndorsementPolicy = 1;


    public Architecture(Entity entity) {
        this.entity = entity;
    }

    public OrderResponse performOrdering(List<RequestData> block){
        //Perform Ordering of the block
        //Update this with the actual ordering logic

        block.sort((o1, o2) -> {
            var o1Time = MiscUtils.fromGoogleTimestampUTC(o1.getTimestamp());
            var o2Time = MiscUtils.fromGoogleTimestampUTC(o2.getTimestamp());
            return o1Time.compareTo(o2Time);
        });

        //TODO: Perform Consensus Here

        return new OrderResponse(true, block);
    }

    public ValidatorResponse performValidation(List<RequestData> block){
        //Perform Validation of the block
        for(var request : block){
           // if current version == request current version then update latest version and set valid field
            // else valid field == false
        }
        //Update this with the actual validation logic
        return new ValidatorResponse(true, "Block is valid", block, List.of());
    }

    public List<RequestData> executeRequests(List<RequestData> block){

        //TODO: Execute requests and save the results
        //TODO: update block with ResultValue and return


        return block;
    }

    public List<RequestData> getBlock(long seqNum){
        var checkpoint = this.entity.getCheckpointManager().getCheckpointForSeq(seqNum);
        return checkpoint.getRequestBlock(seqNum);
    }

    //Default Block Execution : OX Architecture
    public BlockExecResponse executeBlock(long seqNum){
        var block = getBlock(seqNum);
        //Register the block
        //Update this with the actual registration logic
        var orderResponse = performOrdering(block);
        var executedBlock = executeRequests(orderResponse.getOrderedBlock());
        var validatorResponse = performValidation(orderResponse.getOrderedBlock());

        return new BlockExecResponse(
                orderResponse.isSuccess() && validatorResponse.isSuccess(),
                orderResponse,
                validatorResponse
        );
    }

    public RequestData executeRequestAhead(RequestData request){

        return entity.getDataset().executeAhead(request);
    }

    public List<RequestData> executeRequestsAhead(List<RequestData> block){

        List<RequestData> dummyList = new ArrayList<>();

        for (RequestData request : block) {
            dummyList.add(request);
        }

        while(block.size() > 0){
            block.removeFirst();
        }

        for (RequestData req : dummyList){
            block.add( this.executeRequestAhead(req) );
        }

        return block;
    }


    public MessageData createEndorsedMessageToClient(MessageData oldMessage,List<RequestData> requests){
        //Create a new message to be sent to the client
        //Update this with the actual logic
//        entity.l.write(entity.getId(),"g0");
//        var nodesTargetRole = StateMachine.roles.indexOf("client");
//        entity.l.write(entity.getId(),"g1");
//
//        oldMessage = oldMessage.toBuilder().setXovState(1).setIsEndorsementRequest(true).build();
//        entity.l.write(entity.getId(),"g2");
//
//        var clients = this.entity.getRolePlugin().getRoleEntities(
//                oldMessage.getSequenceNum(),
//                oldMessage.getViewNum(),
//                StateMachine.NORMAL_PHASE,
//                nodesTargetRole);
//        entity.l.write(entity.getId(), "clients:"+clients);
//        var targetsList = oldMessage.getTargetsList();
//        entity.l.write(entity.getId(),"g3");
//        targetsList.removeAll(targetsList);
//        entity.l.write(entity.getId(),"g4");
//
//        targetsList.addAll(clients);
//        return oldMessage;
        var nodesTargetRole = StateMachine.roles.indexOf("client");
        var clients = this.entity.getRolePlugin().getRoleEntities(
                oldMessage.getSequenceNum(),
                oldMessage.getViewNum(),
                StateMachine.NORMAL_PHASE,
                nodesTargetRole);
        var newmessage = this.entity.createMessage(oldMessage.getSequenceNum(), oldMessage.getViewNum(), requests, oldMessage.getMessageType(), oldMessage.getSource(), clients);
        newmessage = newmessage.toBuilder().setXovState(2)
                .setIsEndorsementRequest(true)
                .build();

        var targetsList = oldMessage.getTargetsList();
        entity.l.write(entity.getId(),"g3"+ targetsList);
        //var message = createMessage(seqnum, viewNum, block, type, source, targets);
        //message = message.toBuilder().setIsEndorsementRequest(true).setXovState(1).build();
        entity.l.write(entity.getId(),"g5"+newmessage.toString());
        //targetsList.addAll(clients);
        return newmessage;
    }
}
