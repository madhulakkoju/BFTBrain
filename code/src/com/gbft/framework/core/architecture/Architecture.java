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

        List<RequestData> dummyList = new ArrayList<RequestData>();
        dummyList.addAll(block);

        while(!block.isEmpty()){
            block.remove(0);
        }

        for (RequestData request : dummyList) {
            block.add(entity.getDataset().executeAhead(request));
        }
        return block;
    }


    public MessageData createEndorsedMessageToClient(MessageData oldMessage, List<RequestData> requests){
        //Create a new message to be sent to the client
        //Update this with the actual logic
        var targetClients = StateMachine.roles.indexOf("client");

        var clients = this.entity.getRolePlugin().getRoleEntities(
                oldMessage.getSequenceNum(),
                oldMessage.getViewNum(),
                StateMachine.NORMAL_PHASE,
                targetClients);

        var newMessage = this.entity.createMessage(
                oldMessage.getSequenceNum(),
                oldMessage.getViewNum(),
                requests,
                oldMessage.getMessageType(),
                entity.getId(),
                clients
        );
        newMessage = newMessage.toBuilder().setXovState(2)
                .setIsEndorsementRequest(true)
                .build();

        return newMessage;
    }

    public boolean isValidRequest(RequestData request){
        //Check if the request is valid
        //Update this with the actual logic

        if(this.entity.getDataset().getRecordCurrentVersion().get(request.getSender()) != request.getCurrentVersion()){
            request = request.toBuilder().setIsTnxValid(false).build();
            return false;
        }

        return true;
    }
}
