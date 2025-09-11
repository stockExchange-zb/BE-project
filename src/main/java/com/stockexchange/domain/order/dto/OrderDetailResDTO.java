package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResDTO {
    private Long stockId;
    private Long orderId;
    private int orderCount;
    private BigDecimal orderPrice;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private int orderRemainCount;
    private int orderExecutedCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
