package com.gbft.framework.core.architecture;

import com.gbft.framework.data.RequestData;
import lombok.Data;

import java.util.List;

@Data
public class ValidatorResponse {
    public boolean success;
    public String message;

    public List<RequestData> validRequests;
    public List<RequestData> invalidRequests;

    public ValidatorResponse(boolean b, String blockIsValid, List<RequestData> validRequests, List<RequestData> invalidRequests) {
        this.success = b;
        this.message = blockIsValid;
        this.validRequests = validRequests;
        this.invalidRequests = invalidRequests;
    }

}
