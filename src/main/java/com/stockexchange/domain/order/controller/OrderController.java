package com.stockexchange.domain.order.controller;

import com.stockexchange.domain.order.domain.Order;
import com.stockexchange.domain.order.dto.OrderDetailResV1;
import com.stockexchange.domain.order.dto.OrderListResV1;
import com.stockexchange.domain.order.dto.OrderReqV1;
import com.stockexchange.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<OrderListResV1>> getAllOrders(@PathVariable final Long userId) {
        final List<Order> orders = orderService.getAllOrders(userId);

//        Domain -> DTO 변환
        final List<OrderListResV1> response = orders.stream()
                .map(OrderListResV1::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문을 상세 조회합니다.")
    public ResponseEntity<OrderDetailResV1> getOrderById(@PathVariable("userId") final Long userId, @PathVariable("orderId") final Long orderId) {
        final Order order = orderService.getOrderDetail(userId, orderId);

//        Domain -> DTO
        final OrderDetailResV1 response = OrderDetailResV1.from(order);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{userId}/orders")
    @Operation(summary = "주문 등록", description = "주문을 등록합니다.")
    public ResponseEntity<OrderDetailResV1> createOrder(@PathVariable final Long userId, @Valid @RequestBody final OrderReqV1 orderReqV1) {
        final Order order = orderService.createOrder(userId, orderReqV1);

        final OrderDetailResV1 response = OrderDetailResV1.from(order);

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 수정", description = "체결되지 않은 주문을 수정합니다.")
    public ResponseEntity<OrderDetailResV1> updateOrderById(
            @Parameter(description = "사용자 아이디") @PathVariable final Long userId,
            @Parameter(description = "주문 아이디") @PathVariable final Long orderId,
            @Parameter(description = "수정할 주문 정보") @RequestBody @Valid final OrderReqV1 orderReqV1) {
        final Order order = orderService.updateOrder(userId, orderId, orderReqV1);

        final OrderDetailResV1 response = OrderDetailResV1.from(order);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/orders/{orderId}")
    @Operation(summary = "주문 취소", description = "체결되지 않은 주문을 취소합니다.")
    public ResponseEntity<Integer> deleteOrderById(@PathVariable final Long userId, @PathVariable("orderId") final Long orderId) {
        orderService.deleteOrder(userId, orderId);
        return ResponseEntity.ok().body(SUCCESS_DELETE);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content 반환
    }
}