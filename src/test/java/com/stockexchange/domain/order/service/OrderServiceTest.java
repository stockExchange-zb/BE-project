package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import com.stockexchange.domain.order.repository.OrderRepository;
import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks // 테스트 대상 Service
    private OrderService orderService;

    private Long userId;
    private Long orderId;
    private OrderReqDTO mockOrderReqDTO;
    private OrderDetailResDTO mockOrderDetailResDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 1L;
        mockOrderReqDTO = new OrderReqDTO(
                10,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                10L,
                userId
        );

        mockOrderDetailResDTO = new OrderDetailResDTO(
                10L, 1L, 10, new BigDecimal("15000.00"),
                OrderType.BUY, OrderStatus.PENDING, 10, 0,
                ZonedDateTime.now(), ZonedDateTime.now()
        );
    }

    @Test
    @DisplayName("주문 목록 조회 - 목록 전체 조회 성공")
    void getAllOrders_Success() {
//        Given: Mock Repository 동작 정의
        List<OrderListResDTO> mockOrderList = Arrays.asList(
                new OrderListResDTO(1L, 10, OrderType.BUY, OrderStatus.PENDING, 100L, ZonedDateTime.now()),
                new OrderListResDTO(2L, 20, OrderType.SELL, OrderStatus.COMPLETED, 200L, ZonedDateTime.now())
        );

        when(orderRepository.findAllByUserId(userId)).thenReturn(mockOrderList);

//        When: Service 메서드 호출
        List<OrderListResDTO> result = orderService.getAllOrders(userId);

//        Then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(orderId, result.get(0).getOrderId());
    }

    @Test
    @DisplayName("주문 조회 - 특정 주문 조회 성공")
    void getOrderDetail_Success() {
//        Given: Mock Repository 동작 정의
        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(mockOrderDetailResDTO);

//        When: 실제 서비스 메서드 호출
        OrderDetailResDTO result = orderService.getOrderDetail(orderId, userId);

//        Then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockOrderDetailResDTO, result);
    }

    @Test
    @DisplayName("주문 조회 - 주문이 없는 경우")
    void getOrder_NotExist_Fail() {
//        Given: Repository가 null 반환하도록 설정
        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(null);

//        When: 실제 서비스 메서드 호출
        OrderDetailResDTO result = orderService.getOrderDetail(orderId, userId);

//        Then: null 반환 검증
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("주문 등록 - 성공")
    void createOrder_Success() {
//        Given: 테스트 입력 데이터와 Mock 동작 정의
        StockEntity mockStockEntity = Mockito.mock(StockEntity.class);
        OrderEntity mockSavedOrder = Mockito.mock(OrderEntity.class);
//        Mock 동작 정의
        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));
        when(mockSavedOrder.getOrderId()).thenReturn(1L);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockSavedOrder); // save 메서드 호출 시 mockSavedOrder 객체 반환
        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(mockOrderDetailResDTO); // orderId, userId 호출 시 mockOrderDetailResDTO 반환

//        When: 실제 서비스 메서드 호출
        OrderDetailResDTO result = orderService.createOrder(userId, mockOrderReqDTO);

//        Then: 비즈니스 로직 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.getOrderCount());
    }

    @Test
    @DisplayName("주문 등록 테스트 - 존재하지 않는 주식")
    void createOrder_NotExistStock_Fail() {
//        Given : 존재하지 않는 stockId로 설정
        OrderReqDTO mockOrderReqDTO = new OrderReqDTO(
                0,
                new BigDecimal("1500.00"),
                OrderType.SELL,
                999L,
                userId
        );

//        stockRepository가 빈 Optional 반환하도록 설정
        when(stockRepository.findById(999L)).thenReturn(Optional.empty());

//        When & Then : NoSuchElementException 예상
        Assertions.assertThrows(NoSuchElementException.class,
                ()->{orderService.createOrder(userId, mockOrderReqDTO);});
    }

    @Test
    @DisplayName("주문 등록 테스트 - 주문 수량 0인 경우")
    void createOrder_insufficient_False(){
//        Given : 주문 수량 0인 데이터 생성
        OrderReqDTO mockOrderReqDTO = new OrderReqDTO(
                0,
                new BigDecimal("1500.00"),
                OrderType.SELL,
                10L,
                userId
        );

        StockEntity mockStockEntity = Mockito.mock(StockEntity.class);
//        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));
//        수량검증을 이미 수행, OrderService > stockRepository 호출 X

//        When & Then : 유효성 검증 예외 발생
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> {orderService.createOrder(userId, mockOrderReqDTO);});
    }

    @Test
    @DisplayName("주문 등록 테스트 - 음수 가격 주문")
    void createOrder_NegativePrice_False(){
        OrderReqDTO mockOrderReqDTO = new OrderReqDTO(
                0,
                new BigDecimal("-1500.00"),
                OrderType.SELL,
                10L,
                userId
        );

        StockEntity mockStockEntity = Mockito.mock(StockEntity.class);
//        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));

//        When & Then
        Assertions.assertThrows(IllegalArgumentException.class,
                ()->{orderService.createOrder(userId, mockOrderReqDTO);});
    }

    @Test
    void updateOrder() {
    }

    @Test
    void deleteOrder() {
    }
}