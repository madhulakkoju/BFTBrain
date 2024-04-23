package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.MiscUtils;

import java.util.List;

public class Architecture {
    private Entity entity;


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

    //Default Block Execution : OX Architecture
    public BlockExecResponse executeBlock(long seqNum){
        var checkpoint = this.entity.getCheckpointManager().getCheckpointForSeq(seqNum);
        var block = checkpoint.getRequestBlock(seqNum);
        //Register the block
        //Update this with the actual registration logic

        var orderResponse = performOrdering(block);
        var validatorResponse = performValidation(orderResponse.getOrderedBlock());

        return new BlockExecResponse(
                orderResponse.isSuccess() && validatorResponse.isSuccess(),
                orderResponse,
                validatorResponse
        );
    }


}
