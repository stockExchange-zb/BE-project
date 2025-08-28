package com.stockexchange.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Table(name = "holding")
public class HoldingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id", nullable = false)
    private Long holdingId;

    @Column(name = "holding_quantity", nullable = false)
    private int holdingQuantity;

    @Column(name = "holding_total_price", nullable = false)
    private long holdingTotalPrice;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;
}
