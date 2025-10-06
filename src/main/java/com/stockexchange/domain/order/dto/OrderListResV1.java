package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.domain.Order;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.*;

import java.time.ZonedDateTime;

@Builder
@Getter
public class OrderListResV1 {
    private Long orderId;
    private int orderCount;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Long stockId;
    private ZonedDateTime createdAt;

//    Domain -> DTO 변환
    public static OrderListResV1 from(Order order) {
        return OrderListResV1.builder()
                .orderId(order.getOrderId())
                .stockId(order.getStockId())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .orderCount(order.getOrderCount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
