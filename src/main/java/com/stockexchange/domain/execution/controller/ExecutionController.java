package com.stockexchange.domain.execution.controller;

import com.stockexchange.domain.execution.dto.ExecutionResDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {

    @GetMapping
    @Operation(summary = "체결 내역 전체 조회", description = "체결 내역 전체를 조회합니다.")
    public ResponseEntity<List<ExecutionResDTO>> getAllExecutions() {
//        TODO
        return ResponseEntity.ok().body(List.of());
    }

    @GetMapping("/{executionId}")
    @Operation(summary = "체결 상세 조회", description = "특정 체결 내역을 상세 조회합니다.")
    public ResponseEntity<ExecutionResDTO> getExecutionById(@PathVariable("executionId") Long executionId) {
//        TODO
        return ResponseEntity.ok().body(new ExecutionResDTO());
    }
}
