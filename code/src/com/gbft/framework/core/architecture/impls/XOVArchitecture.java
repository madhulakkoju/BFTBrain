package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.Architecture;
import com.gbft.framework.core.architecture.BlockExecResponse;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.ValidatorResponse;
import com.gbft.framework.data.MessageData;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.statemachine.StateMachine;
import com.gbft.framework.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class XOVArchitecture extends Architecture {

    public XOVArchitecture(Entity entity) {
        super(entity);
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

    public List<RequestData> executeRequestsAhead(List<RequestData> block){
        List<RequestData> executeAheadBlock = new ArrayList<>(block.size());
        for (RequestData request : block) {
            executeAheadBlock.add(entity.getDataset().executeAhead(request));
        }
        return executeAheadBlock;
    }

    public RequestData executeRequestAhead(RequestData request){
        return entity.getDataset().executeAhead(request);
    }



    // Block Execution : XOV Architecture
    public BlockExecResponse executeBlock(long seqNum){
        var block = getBlock(seqNum);
        //Register the block
        //Update this with the actual registration logic
        executeRequestsAhead(block); // X
        var orderResponse = performOrdering(block); // O
        var validatorResponse = performValidation(orderResponse.getOrderedBlock()); // V

        return new BlockExecResponse(
                orderResponse.isSuccess() && validatorResponse.isSuccess(),
                orderResponse,
                validatorResponse
        );
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



}
