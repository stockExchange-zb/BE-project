package com.stockexchange.domain.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "stock")
public class Stock {

    @Id
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "stock_number")
    private Integer stockNumber;

    @Column(name = "stock_ipo")
    @Enumerated
    private StockIpo stockIpo;

    @Column(name = "stock_price")
    private Integer stockPrice;
}