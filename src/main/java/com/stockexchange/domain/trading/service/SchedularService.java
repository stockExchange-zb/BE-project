package com.stockexchange.domain.trading.service;

import com.stockexchange.domain.execution.service.ExecutionService;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedularService {

    private final OrderRepository orderRepository;
    private final ExecutionService executionService;

    /* 현재 시간이 거래 가능 시간인지 체크
    거래 가능 시간이면 trun return */
    private boolean isTradingTime(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalTime currentTime = now.toLocalTime();

        LocalTime tradingStartTime = LocalTime.of(9, 0);
        LocalTime tradingEndTime = LocalTime.of(15, 20);

        return !currentTime.isBefore(tradingStartTime) && !currentTime.isAfter(tradingEndTime);
    }

    /* 10초마다 PENDING 주문들을 체결 처리
    * 장 시간(09:00 ~ 15:20) 내에서만 실행 */

    @Scheduled(fixedRate = 10000) // 10초마다 실행
    @Transactional
    public void processOrderMatching(){
//        1. 장 시간 체크
        if(!isTradingTime()){
            log.debug("현재 장 시간 마감입니다. 체결 처리를 실행하지 않습니다.");
            return;
        }

//        2. 주문 매칭 스케줄러 시작
        log.info("주문 매칭 스케줄러 시작");

        try {
//            PENDING 상태의 주문들 조회(시간순 정렬)
            List<OrderEntity> pendingOrders = orderRepository.findPendingOrdersByCreatedAt();

            if(pendingOrders.isEmpty()){
                log.debug("매칭할 주문이 없습니다.");
                return;
            }

//            3. 각 주문에 대해 체결 시도
            int matchCount = 0;
            for(OrderEntity order : pendingOrders){
                boolean executed = executionService.orderExecution(order);
                if(executed){
                    matchCount++;
                }
            }
        } catch (Exception e) {
            log.error("주문 매칭 처리 중 오류 발생: " + e.getMessage());
        }
        log.info("주문 매칭 스케줄러 종료");
    }
}
