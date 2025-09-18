package com.stockexchange.domain.order.entity;

import com.stockexchange.domain.stock.entity.StockEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "order_table")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Min(value = 1, message = "주문 수량은 1 이상 필수입니다.")
    @Column(name = "order_count", nullable = false)
    private int orderCount;

    @Column(name = "order_price", nullable = false)
    private BigDecimal orderPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "order_remain_count", nullable = false)
    private int orderRemainCount;

    @Column(name = "order_executed_count", nullable = false)
    private int orderExecutedCount;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private ZonedDateTime updatedAt;

    @JoinColumn(name = "stock_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private StockEntity stock;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    //    OrderRepositoryTest 위한 생성자
    public OrderEntity(int orderCount, BigDecimal orderPrice, OrderType orderType, OrderStatus orderStatus, int orderRemainCount, int orderExecutedCount, ZonedDateTime createdAt, ZonedDateTime updatedAt, StockEntity stock, Long userId) {
        this.orderCount = orderCount;
        this.orderPrice = orderPrice;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderRemainCount = orderRemainCount;
        this.orderExecutedCount = orderExecutedCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stock = stock;
        this.userId = userId;
    }

    //    Rich Entity : 자신을 생성하는 비즈니스 규칙 포함
    public static OrderEntity createOrder(int orderCount, BigDecimal orderPrice, OrderType orderType, StockEntity stockId, Long userId) {

//        비즈니스 규칙 검증 - Entity에서
        validateStock(stockId); // 종목 거래 가능한지 확인
        validateTradingHours(); // 거래 가능한 시간인지 확인
        validateUserId(userId); // 회원인지 확인

        OrderEntity order = new OrderEntity();
        order.orderCount = orderCount;
        order.orderPrice = orderPrice;
        order.orderType = orderType;
        order.orderStatus = OrderStatus.PENDING;
        order.orderRemainCount = orderCount;
        order.orderExecutedCount = 0;
        order.createdAt = ZonedDateTime.now();
        order.updatedAt = ZonedDateTime.now();
        order.stock = stockId;
        order.userId = userId;
        return order;
    }

    //    Rich Entity : 자신의 상태 변경 규칙 포함
    public void updateOrder(int orderCount, BigDecimal orderPrice) {
//        수정 가능 여부 검증
        validateCanModify(); // PENDING 상태이고, 체결되지 않았는지

        this.orderCount = orderCount;
        this.orderPrice = orderPrice;
        this.orderRemainCount = orderCount; // 수정 시 남은 수량도 새로운 수량으로 재설정
        this.updatedAt = ZonedDateTime.now(); // 수정 시간 업데이트
    }

    //    비즈니스 규칙 검증 메서드==================================================

//    수정 가능 여부 검증 로직
    private void validateCanModify() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new IllegalArgumentException("PENDING 상태의 주문만 수정할 수 있습니다. 현재 상태: " + this.orderStatus);
        }
        if (this.orderExecutedCount > 0) {
            throw new IllegalArgumentException("이미 " + this.orderExecutedCount + "개가 체결되어 주문은 수정할 수 없습니다.");
        }
    }

//    회원 확인 로직
    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("회원만 거래할 수 있습니다.");
        }
    }

//    거래 가능 시간 확인 로직
    private static void validateTradingHours() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalTime currentTime = now.toLocalTime();

        LocalTime tradingStart = LocalTime.of(9, 0);
        LocalTime tradingEnd = LocalTime.of(15, 20);

        if (currentTime.isBefore(tradingStart) || currentTime.isAfter(tradingEnd)) {
            throw new IllegalArgumentException("현재 거래 가능 시간이 아닙니다. 거래 시간: 09:00 ~ 15:20");
        }
    }

//    종목 확인 로직
    private static void validateStock(StockEntity stock) {
        if (stock == null) {
            throw new IllegalArgumentException("종목 정보는 필수입니다.");
        }

    }

//    소유자 검증 로직
    public void validateOwnership(Long requestUserId) {
        if(this.userId.equals(requestUserId)) {
            throw new IllegalArgumentException("주문 수정/취소 권한이 없습니다.");
        }
    }
}
