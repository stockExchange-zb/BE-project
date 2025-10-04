package com.stockexchange.domain.stock.dto;

import com.stockexchange.domain.stock.entity.StockIpo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StockResDTO {
    private Long stockId;
    private String stockName;
    private String stockNumber;
    private StockIpo stockIpo;
    private BigDecimal stockPrice;
}
