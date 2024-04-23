package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.Architecture;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.ValidatorResponse;
import com.gbft.framework.data.RequestData;

import java.util.List;

public class OXArchitecture extends Architecture{


    public OXArchitecture(Entity entity) {
        super(entity);
    }

    public OrderResponse performOrdering(List<RequestData> block){
        //Perform Ordering of the block
        //Update this with the actual ordering logic
        return orderer.order(block);
    }

    public ValidatorResponse performValidation(List<RequestData> block){
        //Perform Validation of the block
        //Update this with the actual validation logic
        return validator.validate(block);
    }
}
