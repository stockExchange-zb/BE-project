package com.stockexchange.domain.stock.controller;

import com.stockexchange.domain.stock.dto.StockResDTO;
import com.stockexchange.domain.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "주식 종목 전체 조회", description = "주식 종목 전체를 조회합니다")
    public ResponseEntity<List<StockResDTO>> getAllStocks() {
        List<StockResDTO> stockResDTOS = stockService.getAllStocks();
        return ResponseEntity.ok(stockResDTOS);
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "종목 상세 조회", description = "특정 종목을 상세 조회합니다")
    public ResponseEntity<StockResDTO> getStockById(@PathVariable Long stockId) {
        StockResDTO stockResDTO = stockService.getStockById(stockId);
        return ResponseEntity.ok(stockResDTO);
    }
}
