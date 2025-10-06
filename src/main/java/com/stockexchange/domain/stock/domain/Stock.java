package com.stockexchange.domain.stock.domain;

import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.entity.StockIpo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Stock {
    private Long stockId;
    private String stockName;
    private String stockNumber;
    private StockIpo stockIpo;
    private BigDecimal stockPrice;

    //    StockEntity -> Stock 도메인 변환
    public static Stock from(StockEntity stockEntity) {
        return Stock.builder()
                .stockId(stockEntity.getStockId())
                .stockName(stockEntity.getStockName())
                .stockNumber(stockEntity.getStockNumber())
                .stockIpo(stockEntity.getStockIpo())
                .stockPrice(stockEntity.getStockPrice())
                .build();
    }
}
