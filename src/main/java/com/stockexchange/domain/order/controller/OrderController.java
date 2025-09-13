package com.stockexchange.domain.order.controller;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class OrderController {

    private static final int SUCCESS_CREATE = 201;
    private static final int SUCCESS_UPDATE = 200;
    private static final int SUCCESS_DELETE = 204;

    private final OrderService orderService;

    @GetMapping("/{userId}/orders")
    @Operation(summary = "주문 전체 조회", description = "주문 목록 전체를 조회합니다")
    public ResponseEntity<List<OrderListResDTO>> getAllOrders(@PathVariable Long userId) {
        List<OrderListResDTO> orderListResDTOList = orderService.getAllOrders(userId);
        return ResponseEntity.ok().body(orderListResDTOList);
    }

    @GetMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문을 상세 조회합니다.")
    public ResponseEntity<OrderDetailResDTO> getOrderById(@PathVariable("userId") Long userId, @PathVariable("orderId") Long orderId) {
        OrderDetailResDTO orderDetailResDTO = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok().body(orderDetailResDTO);
    }

    @PostMapping("/{userId}/orders")
    @Operation(summary = "주문 등록", description = "주문을 등록합니다.")
    public ResponseEntity<OrderDetailResDTO> createOrder(@PathVariable Long userId, @RequestBody OrderReqDTO order) {
        OrderDetailResDTO createOrder = orderService.createOrder(userId, order);
//        return ResponseEntity.ok().body(SUCCESS_CREATE);
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrder);
    }

    @PutMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 수정", description = "체결되지 않은 주문을 수정합니다.")
    public ResponseEntity<OrderDetailResDTO> updateOrderById(
            @Parameter(description = "사용자 아이디") @PathVariable Long userId,
            @Parameter(description = "주문 아이디") @PathVariable Long orderId,
            @Parameter(description = "수정할 주문 정보") @RequestBody @Valid OrderReqDTO orderReqDTO) {
        OrderDetailResDTO updateOrder = orderService.updateOrder(userId, orderId, orderReqDTO);
        return ResponseEntity.ok(updateOrder);
    }

    @DeleteMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 취소", description = "체결되지 않은 주문을 취소합니다.")
    public ResponseEntity<Integer> deleteOrderById(@PathVariable Long userId, @PathVariable("orderId") Long orderId) {
        orderService.deleteOrder(userId, orderId);
        return ResponseEntity.ok().body(SUCCESS_DELETE);
    }
}