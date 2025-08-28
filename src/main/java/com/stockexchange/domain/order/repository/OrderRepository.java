package com.stockexchange.domain.order.repository;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 주문 목록 전체 조회
    @Query("""
            SELECT new com.stockexchange.domain.order.dto.OrderListResDTO(
                        o.orderId,
                        o.orderCount,
                        o.orderType,
                        o.orderStatus,
                        o.stock.stockId,
                        o.createdAt
            )
            FROM OrderEntity o
            WHERE o.userId = :userId
            ORDER BY o.createdAt DESC
            """)
    List<OrderListResDTO> findAllByUserId(@Param("userId") Long userId);

//    특정 주문 상세 조회
    @Query("""
            SELECT new com.stockexchange.domain.order.dto.OrderDetailResDTO(
                    o.stock.stockId,
                    o.orderId,
                    o.orderCount,
                    o.orderPrice,
                    o.orderType,
                    o.orderStatus,
                    o.orderRemainCount,
                    o.orderExecutedCount,
                    o.createdAt,
                    o.updatedAt
            )
            FROM OrderEntity o
            WHERE o.userId = :userId AND o.orderId = :orderId
            ORDER BY o.createdAt DESC
            """)
    OrderDetailResDTO findByOrderIdAndUserId(@Param("userId") Long userId, @Param("orderId")Long orderId);
}