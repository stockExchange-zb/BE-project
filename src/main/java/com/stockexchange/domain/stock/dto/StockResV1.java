package com.stockexchange.domain.stock.dto;

import com.stockexchange.domain.stock.domain.Stock;
import com.stockexchange.domain.stock.entity.StockIpo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StockResV1 {
    private Long stockId;
    private String stockName;
    private String stockNumber;
    private StockIpo stockIpo;
    private BigDecimal stockPrice;

    //    Stock 도메인 -> DTO 변환
    public static StockResV1 from(Stock stock) {
        return StockResV1.builder()
                .stockId(stock.getStockId())
                .stockName(stock.getStockName())
                .stockIpo(stock.getStockIpo())
                .stockNumber(stock.getStockNumber())
                .stockPrice(stock.getStockPrice())
                .build();
    }
}
