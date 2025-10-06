package com.stockexchange.domain.order.repository;

import com.stockexchange.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 주문 목록 전체 조회
    List<OrderEntity> findAllByUserId(@Param("userId") Long userId);

    //    특정 주문 상세 조회
    OrderEntity findByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);
}