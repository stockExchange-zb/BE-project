package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.domain.Order;
import com.stockexchange.domain.order.dto.OrderReqV1;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.repository.OrderRepository;
import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    //    주문 목록 전체 조회
    @Transactional(readOnly = true)
    public List<Order> getAllOrders(Long userId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByUserId(userId);

        return orderEntityList.stream()
                .map(Order::from)
                .collect(Collectors.toList());
    }

    //    특정 주문 상세 조회
    @Transactional(readOnly = true)
    public Order getOrderDetail(Long userId, Long orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(orderId, userId);
        if (orderEntity == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다.: " + orderId);
        }


        return Order.from(orderEntity);
    }

    //    주문 등록
    @Transactional
    public Order createOrder(Long userId, OrderReqV1 orderReqV1) {
//        1. StockEntity  조회
        StockEntity stockEntity = stockRepository.findById(orderReqV1.getStockId())
                .orElseThrow(() -> new IllegalArgumentException("주문하시는 종목이 존재하지 않습니다.: " + orderReqV1.getStockId()));

//        2. createOrder
        OrderEntity orderEntity = OrderEntity.createOrder(
                orderReqV1.getOrderCount(),
                orderReqV1.getOrderPrice(),
                orderReqV1.getOrderType(),
                stockEntity,
                userId
        );

//        3. 저장
        orderRepository.save(orderEntity);

//        4. Entity -> Domain 변환 후 반환
        return Order.from(orderEntity);
    }

    //    주문 수정
    @Transactional
    public Order updateOrder(Long userId, Long orderId, OrderReqV1 orderReqV1) {
//        1. 기존 주문 조회 및 존재 여부 확인
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("수정할 주문을 찾을 수 없습니다."));

//        소유자 검증
        orderEntity.validateOwnership(userId);

//        Entity의 updateOrder 메서드 사용(더티체킹)
        orderEntity.updateOrder(orderReqV1.getOrderCount(), orderReqV1.getOrderPrice());

        //        주문 수정- 더티체킹으로 자동 UPDATE
        /* 더티체킹 : JPA 가 Entity의 변경사항을 자동으로 감지하는 기능
         * @Transactional 안에서 Enttiy를 조회하고 수정하면 자동으로 UPDATE 쿼리 실행
         * orderRepository.save()를 호출하지 않아도 됩니다. */

        return Order.from(orderEntity);
    }

    //    주문 삭제
    @Transactional
    public void deleteOrder(Long userId, Long orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(orderId, userId);

        Order order = Order.from(orderEntity);

//        비즈니스 로직: 취소 가능 여부 체크
        if (!order.canCancel()) {
            throw new IllegalStateException("체결된 주문은 취소할 수 없습니다.");
        }
        orderRepository.deleteById(orderId);
    }
}