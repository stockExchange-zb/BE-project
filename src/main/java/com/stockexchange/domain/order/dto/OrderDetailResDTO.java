package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderDetailResDTO {
    private Long stockId;
    private Long orderId;
    private Integer orderCount;
    private Integer orderPrice;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Integer orderRemainCount;
    private Integer orderExecutedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
