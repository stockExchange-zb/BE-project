package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResDTO {
    private Long orderId;
    private int orderCount;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Long stockId;
    private ZonedDateTime createdAt;
}
