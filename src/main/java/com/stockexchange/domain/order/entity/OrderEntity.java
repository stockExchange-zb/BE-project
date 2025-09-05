package com.stockexchange.domain.order.entity;

import com.stockexchange.domain.stock.entity.StockEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor
@ToString(exclude = {"stock"})
@Table(name = "order_table")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_count", nullable = false)
    private int orderCount;

    @Column(name = "order_price", nullable = false)
    private BigDecimal orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "order_remain_count", nullable = false)
    private int orderRemainCount;

    @Column(name = "order_executed_count", nullable = false)
    private int orderExecutedCount;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @JoinColumn(name = "stock_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private StockEntity stock;

    @Column(name = "user_id", nullable = false)
    private Long userId;

//    OrderRepositoryTest 위한 생성자
    public OrderEntity(int orderCount, BigDecimal orderPrice, OrderType orderType, OrderStatus orderStatus, int orderRemainCount, int orderExecutedCount, ZonedDateTime createdAt, ZonedDateTime updatedAt, StockEntity stock, Long userId) {
        this.orderCount = orderCount;
        this.orderPrice = orderPrice;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderRemainCount = orderRemainCount;
        this.orderExecutedCount = orderExecutedCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stock = stock;
        this.userId = userId;
    }
}
