package com.stockexchange.domain.execution.entity;

import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.stock.entity.StockEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"buyOrder", "sellOrder", "stock"})
@Table(name = "execution")
public class ExecutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @Column(name = "execution_count", nullable = false)
    private int executionCount;

    @Column(name = "execution_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal executionPrice;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_buy_order_id", nullable = true)
    private OrderEntity buyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_sell_order_id", nullable = true)
    private OrderEntity sellOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private StockEntity stock;

    /* 체결 생성 정적 팩토리 메서드
    * 1. 생성 로직을 캡슐화하여 Entity 내부에서 관리
    * 2. 메서드 이름으로 의도 명확히 표현(createExecution)
    * 3. 생성 시 필수 검증 로직을 강제할 수 있음
    * 4. 외부에서 new ExecutionEntity() 로 잘못된 객체 생성 방지 */
    public static ExecutionEntity createExecution(OrderEntity buyOrder, OrderEntity sellOrder, int executionCount, BigDecimal executionPrice, StockEntity stock) {

//   비즈니스 규칙 검증
        validateExecution(buyOrder, sellOrder, executionCount, stock);
        return ExecutionEntity.builder()
                .buyOrder(buyOrder)
                .sellOrder(sellOrder)
                .executionCount(executionCount)
                .executionPrice(executionPrice)
                .stock(stock)
                .createdAt(ZonedDateTime.now())
                .build();
    }

    /* 체결 생성 시 검증 로직 */
    private static void validateExecution(OrderEntity buyOrder, OrderEntity sellOrder, int executionCount, StockEntity stock) {
        if (buyOrder == null || sellOrder == null) {
            throw new IllegalArgumentException("매수/매도 주문은 필수입니다.");
        }
        if(stock == null){
            throw new IllegalArgumentException("종목 정보는 필수입니다.");
        }
        if(executionCount <= 0){
            throw new IllegalArgumentException("체결 수량은 1 이상이어야 합니다.");
        }
        if(!buyOrder.getStock().getStockId().equals(sellOrder.getStock().getStockId())){
            throw new IllegalArgumentException("서로 다른 종목의 주문은 체결될 수 없습니다.");
        }
        if(buyOrder.getOrderRemainCount() < executionCount || sellOrder.getOrderRemainCount() < executionCount){
            throw new IllegalArgumentException("체결 수량이 남은 주문 수량을 초과합니다.");
        }
    }
}
