package com.stockexchange.domain.stock.service;

import com.stockexchange.domain.stock.repository.StockRepository;
import com.stockexchange.domain.stock.dto.StockResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

//    종목 전체 조회
    @Transactional(readOnly = true)
    public List<StockResDTO> getAllStocks() {
        return stockRepository.findAllStocks();
    }

//    종목 상세 조회
    @Transactional(readOnly = true)
    public StockResDTO getStockById(Long stockId) {
        return stockRepository.findByStockId(stockId);
    }
}
