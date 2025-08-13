package com.stockexchange.domain.stock.controller;

import com.stockexchange.domain.stock.dto.StockResDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stokcs")
public class StockController {

    @GetMapping
    @Operation(summary = "주식 종목 전체 조회", description = "주식 종목 전체 목록을 조회합니다.")
    public ResponseEntity<List<StockResDTO>> getAllStocks() {
//        TODO
        return ResponseEntity.ok().body(new ArrayList<StockResDTO>());
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "특정 종목 조회", description = "특정 종목을 상세 조회합니다.")
    public ResponseEntity<StockResDTO> getStockById(@PathVariable Long stockId) {
//        TODO
        return ResponseEntity.ok().body(new StockResDTO());
    }
}
