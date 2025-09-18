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
//                Stock 조회
//        1. 필요한 데이터 조회(흐름 제어)
        StockEntity stock = stockRepository.findById(orderReqDTO.getStockId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 종목입니다. : " + orderReqDTO.getStockId()));

//        2. Entity 에게 자신의 생성을 위임(Entity가 비즈니스 규칙 처리)
        OrderEntity order = OrderEntity.createOrder(
                orderReqDTO.getOrderCount(),
                orderReqDTO.getOrderPrice(),
                orderReqDTO.getOrderType(),
                stock,
                userId
        );

//        3. 저장(흐름 제어)
        OrderEntity savedOrder = orderRepository.save(order);

//        4. 응답 데이터 조회 및 반환(흐름 제어)
        return orderRepository.findByOrderIdAndUserId(userId, savedOrder.getOrderId());
    }

    //    주문 수정
    @Transactional
    public OrderDetailResDTO updateOrder(Long userId, Long orderId, OrderReqDTO orderReqDTO) {
//        1. 기존 주문 조회 및 존재 여부 확인
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("수정할 주문을 찾을 수 없습니다."));

        //        주문 수정- 더티체킹으로 자동 UPDATE
        /* 더티체킹 : JPA 가 Entity의 변경사항을 자동으로 감지하는 기능
        * @Transactional 안에서 Enttiy를 조회하고 수정하면 자동으로 UPDATE 쿼리 실행
        * orderRepository.save()를 호출하지 않아도 됩니다. */

//        2. Entity에게 권한 검증 위임
        order.validateOwnership(userId);

//        3. Entity에게 수정 로직 위임(Entity가 비즈니스 규칙 처리)
        order.updateOrder(orderReqDTO.getOrderCount(), orderReqDTO.getOrderPrice());

//        4. 응답 데이터 반환(흐름 제어) - 더티체킹으로 자동 저장됨
        return orderRepository.findByOrderIdAndUserId(userId, order.getOrderId());
    }

    //    주문 삭제
    public OrderReqDTO deleteOrder(Long userId, Long orderId) {
        return null;
    }
}