package com.stockexchange.domain.order.controller;

import com.stockexchange.domain.order.dto.OrderResDTO;
import com.stockexchange.domain.order.dto.OrderUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping
    @Operation(summary = "주문 전체 조회", description = "주문 목록 전체를 조회합니다")
    public ResponseEntity<List<OrderResDTO>> getAllOrders() {
//        TODO
        return ResponseEntity.ok().body(List.of());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문을 상세 조회합니다.")
    public ResponseEntity<OrderResDTO> getOrderById(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok().body(new OrderResDTO());
    }

    @PostMapping
    @Operation(summary = "주문 등록", description = "주문을 등록합니다.")
    public ResponseEntity<Integer> createOrder(@RequestBody OrderResDTO order) {
//        TODO
        return ResponseEntity.ok(201);
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "주문 수정", description = "체결되지 않은 주문을 수정합니다.")
    public ResponseEntity<OrderResDTO> updateOrderById(@Parameter(description = "주문 아이디")
                                                       @PathVariable Long orderId,
                                                       @RequestBody OrderUpdateDTO orderUpdateDTO) {
//        TODO
        return ResponseEntity.ok().body(new OrderResDTO());
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "체결되지 않은 주문을 취소합니다.")
    public ResponseEntity<Integer> deleteOrderById(@PathVariable("orderId") String orderId) {
//        TODO
        return ResponseEntity.ok().body(201);
    }
}
