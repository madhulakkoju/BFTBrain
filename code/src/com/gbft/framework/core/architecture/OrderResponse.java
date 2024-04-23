package com.gbft.framework.core.architecture;

import com.gbft.framework.data.RequestData;
import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    public boolean success;
    public List<RequestData> orderedBlock;

    public OrderResponse(boolean success, List<RequestData> orderedBlock) {
        this.success = success;
        this.orderedBlock = orderedBlock;
    }
}
