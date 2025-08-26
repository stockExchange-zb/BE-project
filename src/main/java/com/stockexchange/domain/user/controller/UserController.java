package com.stockexchange.domain.user.controller;

import com.stockexchange.domain.user.dto.HoldingResDTO;
import com.stockexchange.domain.user.dto.UserKRWResDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{userId}/holding")
    @Operation(summary = "보유 주식 전체 조회", description = "보유한 주식 전체를 조회합니다.")
    public ResponseEntity<List<HoldingResDTO>> getAllHoldings(@PathVariable Long userId) {
//        TODO
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{userId}/holding/{holdingId}")
    @Operation(summary = "보유 주식 상세 조회", description = "특정 보유주식을 상세 조회합니다.")
    public ResponseEntity<HoldingResDTO> getHoldingById(@PathVariable Long userId, @PathVariable("holdingId") Long holdingId) {
//        TODO
        return ResponseEntity.ok(new HoldingResDTO());
    }

    @GetMapping("/{userId}/krw")
    @Operation(summary = "보유 현금 조회", description = "보유 현금을 조회합니다.")
    public ResponseEntity<UserKRWResDTO> getHoldingKRW(@PathVariable("userId") Long userId) {
//        TODO
        return ResponseEntity.ok(new UserKRWResDTO());
    }
}
