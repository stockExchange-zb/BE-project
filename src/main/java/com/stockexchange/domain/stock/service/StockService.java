package com.stockexchange.domain.stock.service;

import com.stockexchange.domain.stock.domain.Stock;
import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    //    종목 전체 조회
    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {

        List<StockEntity> stockEntityList = stockRepository.findAll();

        return stockEntityList.stream()
                .map(Stock::from) // StockEntity -> Stock 변환
                .collect(Collectors.toList());
    }

    //    종목 상세 조회
    @Transactional(readOnly = true)
    public Stock getStockById(Long stockId) {
        StockEntity stockEntity = stockRepository.findById(stockId)
                .orElseThrow(() -> new NoSuchElementException("찾는 주식 종목이 존재하지 않습니다." + stockId));

        return Stock.from(stockEntity); // 단일 객체 변환
    }
}
