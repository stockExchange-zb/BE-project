package com.stockexchange.domain.order.repository;

import com.stockexchange.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 주문 목록 전체 조회
    List<OrderEntity> findAllByUserId(@Param("userId") Long userId);

    //    특정 주문 상세 조회
    OrderEntity findByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    /* 매수 주문에 매칭할 매도 주문 찾기
     * 조건
     * - 같은 종목
     * - 매도 타입
     * - 매도 가격 <= 매수 가격(체결 가능)
     * - PENDING 상태
     * - 남은 수량 > 0
     *
     * - 낮은 가격 우선(가격 우선 원칙)
     * - 같은 가격이면 시간 우선 (시간 우선 원칙) */
    @Query("""
            SELECT o FROM OrderEntity o
                       WHERE o.stock.stockId = :stockId
                       AND o.orderType = 'SELL'
                       AND o.orderStatus = 'PENDING'
                       AND o.orderRemainCount > 0
                       AND o.orderPrice <= :buyPrice
                       ORDER BY o.orderPrice ASC, o.createdAt ASC
            """)
    List<OrderEntity> findSellOrdersForMatching(@Param("stockId") Long stockId, @Param("orderPrice") BigDecimal orderPrice);

    /* 매도 주문에 매칭할 매수 주문 찾기
     * 조건
     * - 같은 종목
     * - 매수 타입
     * - 매수 가격 >= 매도 가격(체결 가능)
     * - PENDING 상태
     * - 남은 수량 > 0
     *
     * - 낮은 가격 우선(가격 우선 원칙)
     * - 같은 가격이면 시간 우선 (시간 우선 원칙) */
    @Query("""
            SELECT o FROM OrderEntity o
                       WHERE o.stock.stockId = :stockId
                       AND o.orderType = 'BUY'
                       AND o.orderStatus = 'PENDING'
                       AND o.orderRemainCount > 0
                       AND o.orderPrice >= :sellPrice
                       ORDER BY o.orderPrice ASC, o.createdAt ASC
            """)
    List<OrderEntity> findBuyOrdersForMatching(@Param("stockId") Long stockId, @Param("orderPrice") BigDecimal orderPrice);
}