package com.stockexchange.domain.execution.dto;

import com.stockexchange.domain.execution.domain.Execution;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@Getter
public class ExecutionResV1 {
    private Long executionId;
    private String stockName;
    private int executionCount;
    private BigDecimal executionPrice;
    private ZonedDateTime createdAt;

    public static ExecutionResV1 from(Execution execution) {
        return ExecutionResV1.builder()
                .executionId(execution.getExecutionId())
                .stockName(execution.getStockName())
                .executionCount(execution.getExecutionCount())
                .executionPrice(execution.getExecutionPrice())
                .createdAt(execution.getCreatedAt())
                .build();
    }
}
