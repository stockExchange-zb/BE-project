package com.stockexchange.domain.execution.entity;

import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.stock.entity.StockEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name="execution")
public class ExecutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @Column(name = "execution_count", nullable = false)
    private int executionCount;

    @Column(name = "execution_price", nullable = false)
    private long executionPrice;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="execution_buy_order_id", nullable = true)
    private OrderEntity buyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_sell_order_id", nullable = true)
    private OrderEntity sellOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id", nullable = false)
    private StockEntity stock;
}
