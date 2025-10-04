package com.stockexchange.domain.order.domain;

import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import com.stockexchange.domain.stock.entity.StockEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Builder
@Getter
public class Order {
    private Long orderId;
    private Long userId;
    private Long stockId;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private BigDecimal orderPrice;
    private int orderCount;
    private int orderRemainCount;
    private int orderExecutedCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    //    주문 수정 가능 여부 체크
    public boolean canModify() {
        return this.orderStatus == OrderStatus.PENDING && this.orderExecutedCount == 0;
    }

    //    주문 취소 가능 여부 체크
    public boolean canCancel() {
        return this.orderStatus == OrderStatus.PENDING && this.orderExecutedCount == 0;
    }

    //    Entity -> Domain 변환
    public static Order from(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(orderEntity.getOrderId())
                .userId(orderEntity.getUserId())
                .stockId(orderEntity.getStock().getStockId())
                .orderType(orderEntity.getOrderType())
                .orderStatus(orderEntity.getOrderStatus())
                .orderPrice(orderEntity.getOrderPrice())
                .orderCount(orderEntity.getOrderCount())
                .orderRemainCount(orderEntity.getOrderRemainCount())
                .orderExecutedCount(orderEntity.getOrderExecutedCount())
                .createdAt(orderEntity.getCreatedAt())
                .updatedAt(orderEntity.getUpdatedAt())
                .build();
    }

    //    Domain -> Entity 변환 (생성/수정 시)
    public OrderEntity toEntity(StockEntity stockEntity) {
        OrderEntity.OrderEntityBuilder builder = OrderEntity.builder()
                .userId(this.userId)
                .orderCount(this.orderCount)
                .orderPrice(this.orderPrice)
                .orderType(this.orderType)
                .orderStatus(this.orderStatus != null ? this.orderStatus : OrderStatus.PENDING)
                .orderRemainCount(this.orderRemainCount)
                .orderExecutedCount(this.orderExecutedCount)
                .stock(stockEntity);

//         수정 시에만 orderId 설정
        if (this.orderId != null) {
            builder.orderId(this.orderId);
        }
        return builder.build();

    }
}
