package com.stockexchange.domain.stock.controller;

import com.stockexchange.domain.stock.domain.Stock;
import com.stockexchange.domain.stock.dto.StockResV1;
import com.stockexchange.domain.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "주식 종목 전체 조회", description = "주식 종목 전체를 조회합니다")
    public ResponseEntity<List<StockResV1>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();

        List<StockResV1> stockResV1List = stocks.stream()
                .map(StockResV1::from) // Stock -> StockResV1 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(stockResV1List);
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "종목 상세 조회", description = "특정 종목을 상세 조회합니다")
    public ResponseEntity<StockResV1> getStockById(@PathVariable Long stockId) {
        Stock stock = stockService.getStockById(stockId);
        return ResponseEntity.ok(StockResV1.from(stock));
    }
}
