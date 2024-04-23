package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.RequestData;

import java.util.List;

public class Architecture {

    public Orderer orderer;
    public Validator validator;

    private Entity entity;


    public Architecture(Entity entity) {
        this.orderer = new Orderer();
        this.validator = new Validator();
        this.entity = entity;
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
