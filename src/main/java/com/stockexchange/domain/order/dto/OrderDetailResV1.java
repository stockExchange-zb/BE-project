package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.domain.Order;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@Getter
public class OrderDetailResV1 {
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

    //    Domain -> DTO 변환
    public static OrderDetailResV1 from(Order order) {
        return OrderDetailResV1.builder()
                .stockId(order.getStockId())
                .orderId(order.getOrderId())
                .orderCount(order.getOrderCount())
                .orderPrice(order.getOrderPrice())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .orderRemainCount(order.getOrderRemainCount())
                .orderExecutedCount(order.getOrderExecutedCount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
