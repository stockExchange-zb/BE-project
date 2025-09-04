package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderReqDTO {
    private int orderCount;
    private BigDecimal orderPrice;
    private OrderType orderType;
    private Long stockId;
    private Long userId;
}
