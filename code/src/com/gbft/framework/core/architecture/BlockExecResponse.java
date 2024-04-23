package com.gbft.framework.core.architecture;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockExecResponse {
    public boolean success;
    public LocalDateTime timestamp;

    public OrderResponse orderResponse;
    public ValidatorResponse validatorResponse;

    public BlockExecResponse(boolean success, OrderResponse orderResponse, ValidatorResponse validatorResponse) {
        this.success = success;
        this.timestamp = LocalDateTime.now();
        this.orderResponse = orderResponse;
        this.validatorResponse = validatorResponse;
    }
}
