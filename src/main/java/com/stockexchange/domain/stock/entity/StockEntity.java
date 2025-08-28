package com.stockexchange.domain.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "stock")
public class StockEntity {

    @Id
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_number", nullable = false)
    private String stockNumber;

    @Column(name = "stock_ipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockIpo stockIpo;

    @Column(name = "stock_price", nullable = false)
    private long stockPrice;
}