package com.stockexchange.domain.execution.service;

import com.stockexchange.domain.execution.domain.Execution;
import com.stockexchange.domain.execution.entity.ExecutionEntity;
import com.stockexchange.domain.execution.repository.ExecutionRepository;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import com.stockexchange.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/* 체결 처리, 저장, 상태 업데이트 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public boolean orderExecution(OrderEntity orderEntity) {
        try {
//            1. 주문이 체결 가능한 상태인지 확인
            if (!isExecutable(orderEntity)) {
                return false;
            }
//            2. 주문 타입에 따라 반대편 주문 조회
            List<OrderEntity> findOrders = findOppositeOrders(orderEntity);

            if (findOrders.isEmpty()) {
                log.debug("매칭 가능한 주문이 없습니다.: {}", orderEntity.getOrderId());
                return false;
            }

//            3. 주문 매칭 시도
            boolean executed = false;
            for (OrderEntity tryOrder : findOrders) {
//                현재 주문이 완전히 체결되었으면 종료
                if (orderEntity.getOrderRemainCount() == 0) {
                    break;
                }
//                매칭 가능 여부 확인
                if (canMatch(orderEntity, tryOrder)) {
                    executeMatch(orderEntity, tryOrder);
                    executed = true;
                }
            }
            return executed;

        } catch (Exception e) {
            log.error("주문 체결 처리 중 오류 발생: {}, Error: {}", orderEntity.getOrderId(), e.getMessage(), e);
            throw e;
//            트랜잭션 롤백 위해 예외 던짐
        }
    }

    /* 주문이 체결 가능한 상태인지 확인
    체결 불가능 경우
    - 이미 완전히 체결(COMPLETED)
    - 취소 (CANCELLED)
    - 남은 수량 0
    * */
    private boolean isExecutable(OrderEntity order) {
        return order.getOrderRemainCount() > 0 && order.getOrderStatus() == OrderStatus.PENDING;
    }

    /* 반대편 주문 조회
    * 1. 같은 종목인지 확인
    * 2. 반대 타입 확인 (매수 - 매도)
    * 3. PENDING 상태
    * 4. 남은 수량 > 0
    * 5. 정렬: 가격 우선, 시간 우선
    *
    * - 매수: 높은 가격 우선
    * - 매도: 낮은 가격 우선
    * - 같은 가격: 먼저 들어온 주문 우선
    * */
    private List<OrderEntity> findOppositeOrders(OrderEntity order) {
        Long stockId = order.getStock().getStockId();
        OrderType orderType = order.getOrderType() == OrderType.BUY ? OrderType.SELL : OrderType.BUY;

        if(order.getOrderType() == OrderType.BUY) {
//            매수 주문 -> 매도 주문 찾기 (낮은 가격 우선)
            return orderRepository.findSellOrdersForMatching(stockId, order.getOrderPrice());
        } else {
//            매도 주문 -> 매수 주문 찾기 (높은 가격 우선)
            return orderRepository.findBuyOrdersForMatching(stockId, order.getOrderPrice());
        }
    }

    /* 두 주문 매칭 가능한지 확인
    * - 매칭 조건:
    * 매수 가격 >= 매도 가격*/
    private boolean canMatch(OrderEntity order1, OrderEntity order2) {
        if(order1.getOrderType() == OrderType.BUY) {
//            매수 가격이 매도 가격보다 크거나 같으면 체결
            return order1.getOrderPrice().compareTo(order2.getOrderPrice()) >= 0;
        } else {
//            매도 가격이 매수 가격보다 작거나 같으면 체결
            return order2.getOrderPrice().compareTo(order1.getOrderPrice()) <= 0;
        }
    }

    /* 실제 체결 실행
    *
    * 체결 과정
    * 1. 체결 수량 결정(두 주문 중 작은 수량)
    * 2. 체결 가격 설정(매도자 제시 가격)
    * 3. 체결 기록 생성
    * 4. 양쪽 주문 상태 업데이트
    *
    * 매도자 제시 가격으로 체결 이유
    * - 실제 주식시장 규칙
    * - 호가창에 먼저 있던 주문의 가격 우선 */
    private void executeMatch(OrderEntity buyOrder, OrderEntity sellOrder) {
//        1. 체결 수량 계산(둘 중 작은 값)
        int executionCount = Math.min(buyOrder.getOrderRemainCount(), sellOrder.getOrderRemainCount());

//        2. 체결 가격 결정(매도자 가격)
        BigDecimal executionPrice = sellOrder.getOrderPrice();

//        3. 체결 기록 생성 및 저장
        ExecutionEntity executionEntity = ExecutionEntity.createExecution(
                buyOrder,
                sellOrder,
                executionCount,
                executionPrice,
                buyOrder.getStock()
        );
        executionRepository.save(executionEntity);

//        4. 주문 상태 업데이트
        updateOrderAfterExecution(buyOrder, executionCount);
        updateOrderAfterExecution(sellOrder, executionCount);
    }

    /* 체결 후 주문 상태 업데이트
    *
    * 업데이트 내용
    * 1. 체결된 수량 증가
    * 2. 남은 수량 감소
    * 3. 완전히 체결되면 상태를 COMPLETED로 변경 */
    private void updateOrderAfterExecution(OrderEntity order, int executionCount) {
        int newExecutedCount = order.getOrderExecutedCount() + executionCount;
        int newRemainCount = order.getOrderRemainCount() - executionCount;
        OrderStatus newStatus = (newExecutedCount == 0) ? OrderStatus.COMPLETED : OrderStatus.PENDING;
//        완전히 체결되었는지 확인

        order.updateExecutionInfo(newExecutedCount, newRemainCount, newStatus);

//        주문 정보 업데이트
        orderRepository.save(order);

    }

//    사용자 모든 체결 내역 조회
    @Transactional(readOnly = true)
    public List<Execution> getAllExecutions(Long userId) {
        List<ExecutionEntity> executionEntityList = executionRepository.findAllById(userId);
        return executionEntityList.stream()
                .map(Execution::from)
                .collect(Collectors.toList());
    }
}
