package com.stockexchange.domain.execution.controller;

import com.stockexchange.domain.execution.domain.Execution;
import com.stockexchange.domain.execution.dto.ExecutionResV1;
import com.stockexchange.domain.execution.service.ExecutionService;
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
@RequestMapping("/api/v1/users")
public class ExecutionController {

    private final ExecutionService executionService;

    @GetMapping("/{userId}/execution")
    @Operation(summary = "체결 내역 전체 조회", description = "체결 내역 전체를 조회합니다.")
    public ResponseEntity<List<ExecutionResV1>> getAllExecutions(@PathVariable Long userId) {
        final List<Execution> executions = executionService.getAllExecutions(userId);

        final List<ExecutionResV1> response = executions.stream()
                .map(ExecutionResV1::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{userId}/execution/{executionId}")
    @Operation(summary = "체결 상세 조회", description = "특정 체결 내역을 상세 조회합니다.")
    public ResponseEntity<ExecutionResV1> getExecutionById(@PathVariable("userId") Long userId, @PathVariable("executionId") Long executionId) {
//        TODO
        return ResponseEntity.ok().body(new ExecutionResV1());
    }
}
