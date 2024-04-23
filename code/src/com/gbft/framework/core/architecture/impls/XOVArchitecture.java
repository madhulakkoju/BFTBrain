package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.Architecture;
import com.gbft.framework.core.architecture.BlockExecResponse;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.ValidatorResponse;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.MiscUtils;

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
        for (RequestData request : block) {
            entity.getDataset().executeAhead(request);
        }
        return block;
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



}
