package com.gbft.framework.utils;

import com.gbft.framework.data.Operation;
import com.gbft.framework.data.RequestData;

public class RequestUtils {

    public static Operation getOperation(RequestData requestData) {

        if(!requestData.getWriteSetList().isEmpty()){
            return requestData.getWriteSetList().getFirst().getOp();
        }
        if(!requestData.getReadSetList().isEmpty()){
            return requestData.getReadSetList().getFirst().getOp();
        }
        return Operation.NOP;
    }

    public static int getRecord( RequestData requestData) {

        if(!requestData.getWriteSetList().isEmpty()){
            return requestData.getWriteSetList().getFirst().getRecord();
        }
        if(!requestData.getReadSetList().isEmpty()){
            return requestData.getReadSetList().getFirst().getRecord();
        }
        return -1;
    }
}
