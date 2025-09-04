package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

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
    public OrderDetailResDTO createOrder(Long userId, OrderReqDTO order) {
        return null;
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