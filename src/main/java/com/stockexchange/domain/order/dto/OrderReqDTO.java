package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderReqDTO {
    @Min(value = 1)
    private int orderCount;
    @DecimalMin(value = "0.01")
    private BigDecimal orderPrice;
    private OrderType orderType;
    private Long stockId;
    private Long userId;
}
