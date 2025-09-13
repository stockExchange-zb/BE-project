package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.repository.OrderRepository;
import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    //    주문 목록 전체 조회
    @Transactional(readOnly = true)
    public List<OrderListResDTO> getAllOrders(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    //    특정 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderDetailResDTO getOrderDetail(Long userId, Long orderId) {
        return orderRepository.findByOrderIdAndUserId(userId, orderId);
    }

    //    주문 등록
    @Transactional
    public OrderDetailResDTO createOrder(Long userId, OrderReqDTO orderReqDTO) {
//        유효성 검증
        if (orderReqDTO.getOrderCount() <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상 필수입니다.");
        }

        if (orderReqDTO.getOrderPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("주문 가격은 0보다 커야합니다.");
        }

//                Stock 조회
        StockEntity stock = stockRepository.findById(orderReqDTO.getStockId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 종목입니다. : " + orderReqDTO.getStockId()));

        OrderEntity order = OrderEntity.createOrder(
                orderReqDTO.getOrderCount(),
                orderReqDTO.getOrderPrice(),
                orderReqDTO.getOrderType(),
                stock,
                userId
        );

        OrderEntity savedOrder = orderRepository.save(order);

        return orderRepository.findByOrderIdAndUserId(userId, savedOrder.getOrderId());
    }

    //    주문 수정
    @Transactional
    public OrderDetailResDTO updateOrder(Long userId, Long orderId, OrderReqDTO orderReqDTO) {
//        1. 기존 주문 조회 및 존재 여부 확인
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("수정할 주문을 찾을 수 없습니다."));
//        2. 해당 유저 확인
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
//        3. 주문 상태 - PENDING만 수정 가능
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("주문 상태는 PENDING만 수정 가능합니다.");
        }

//        4. 주문 상태 확인 - 부분 체결 경우 수정 X
        if (order.getOrderExecutedCount() > 0) {
            throw new RuntimeException("이미 체결된 주문은 수정할 수 없습니다.");
        }
//        5. 주문 수정 - 수량은 1 이상 필수
        if (orderReqDTO.getOrderCount() <= 0) {
            throw new RuntimeException("주문 수량은 1 이상 필수 입니다.");
        }
//        6. 주문 가격 - 가격은 양수 필수
        if (orderReqDTO.getOrderPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("주문 가격은 0보다 커야 합니다.");
        }
//        주문 수정- 더티체킹으로 자동 UPDATE
        /* 더티체킹 : JPA 가 Entity의 변경사항을 자동으로 감지하는 기능
        * @Transactional 안에서 Enttiy를 조회하고 수정하면 자동으로 UPDATE 쿼리 실행
        * orderRepository.save()를 호출하지 않아도 됩니다. */
        order.updateOrder(orderReqDTO.getOrderCount(), orderReqDTO.getOrderPrice());

        return orderRepository.findByOrderIdAndUserId(userId, order.getOrderId());
    }

    //    주문 삭제
    public OrderReqDTO deleteOrder(Long userId, Long orderId) {
        return null;
    }
}