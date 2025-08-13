package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderListResDTO {
    private Long orderId;
    private Integer orderCount;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Long stockId;
    private LocalDateTime createdAt;
}
