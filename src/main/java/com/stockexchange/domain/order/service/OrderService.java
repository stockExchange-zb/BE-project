package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.entity.OrderEntity;
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
        if(orderReqDTO.getOrderCount() <= 0){
            throw new IllegalArgumentException("주문 수량은 1 이상 필수입니다.");
        }

        if(orderReqDTO.getOrderPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("주문 가격은 0보다 커야합니다.");
        }

//                Stock 조회
        StockEntity stock = stockRepository.findById(orderReqDTO.getStockId()).orElseThrow();

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
    public OrderReqDTO updateOrder(Long userId, Long orderId) {
        return null;
    }

    //    주문 삭제
    public OrderReqDTO deleteOrder(Long userId, Long orderId) {
        return null;
    }
}