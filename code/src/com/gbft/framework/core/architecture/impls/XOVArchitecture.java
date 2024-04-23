package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.Architecture;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.ValidatorResponse;
import com.gbft.framework.data.RequestData;

import java.util.List;

public class XOVArchitecture extends Architecture {

    public XOVArchitecture(Entity entity) {
        super(entity);
    }

    public ValidatorResponse validate(List<RequestData> block) {
        return validator.validate(block);
    }

    public OrderResponse performOrdering(List<RequestData> block){
        //Perform Ordering of the block

        return orderer.order(block);
    }

}
