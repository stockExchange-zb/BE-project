package com.stockexchange.domain.execution.domain;

import com.stockexchange.domain.execution.entity.ExecutionEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@Getter
public class Execution {
    private Long executionId;
    private Long stockId;
    private String stockName;
    private Long buyOrderId;
    private Long buyUserId;
    private Long sellOrderId;
    private Long sellUserId;
    private int executionCount;
    private BigDecimal executionPrice;
    private ZonedDateTime createdAt;

    /* Entity -> Domain */
    public static Execution from(ExecutionEntity executionEntity) {
        return Execution.builder()
                .executionId(executionEntity.getExecutionId())
                .stockId(executionEntity.getStock().getStockId())
                .stockName(executionEntity.getStock().getStockName())
                .buyOrderId(executionEntity.getBuyOrder().getOrderId())
                .buyUserId(executionEntity.getBuyOrder().getUserId())
                .sellOrderId(executionEntity.getSellOrder().getOrderId())
                .sellUserId(executionEntity.getSellOrder().getUserId())
                .executionCount(executionEntity.getExecutionCount())
                .executionPrice(executionEntity.getExecutionPrice())
                .createdAt(executionEntity.getCreatedAt())
                .build();
    }
}
