package com.stockexchange.domain.order.service;

import com.stockexchange.domain.order.dto.OrderDetailResV1;
import com.stockexchange.domain.order.dto.OrderListResV1;
import com.stockexchange.domain.order.dto.OrderReqV1;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private OrderReqV1 mockOrderReqV1;
    private OrderDetailResV1 mockOrderDetailResV1;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 1L;
        mockOrderReqV1 = new OrderReqV1(
                10,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                10L,
                userId
        );

        mockOrderDetailResV1 = new OrderDetailResV1(
                10L, 1L, 10, new BigDecimal("15000.00"),
                OrderType.BUY, OrderStatus.PENDING, 10, 0,
                ZonedDateTime.now(), ZonedDateTime.now()
        );
    }

    @Test
    @DisplayName("주문 목록 조회 - 목록 전체 조회 성공")
    void getAllOrders_Success() {
//        Given: Mock Repository 동작 정의
        List<OrderListResV1> mockOrderList = Arrays.asList(
                new OrderListResV1(1L, 10, OrderType.BUY, OrderStatus.PENDING, 100L, ZonedDateTime.now()),
                new OrderListResV1(2L, 20, OrderType.SELL, OrderStatus.COMPLETED, 200L, ZonedDateTime.now())
        );

        when(orderRepository.findAllByUserId(userId)).thenReturn(mockOrderList);

//        When: Service 메서드 호출
        List<OrderListResV1> result = orderService.getAllOrders(userId);

//        Then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(orderId, result.get(0).getOrderId());
    }

    @Test
    @DisplayName("주문 조회 - 특정 주문 조회 성공")
    void getOrderDetail_Success() {
//        Given: Mock Repository 동작 정의
        when(orderRepository.findByOrderIdAndUserId(userId, orderId)).thenReturn(mockOrderDetailResV1);

//        When: 실제 서비스 메서드 호출
        OrderDetailResV1 result = orderService.getOrderDetail(userId, orderId);

//        Then: 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockOrderDetailResV1, result);
    }

    @Test
    @DisplayName("주문 조회 - 주문이 없는 경우")
    void getOrder_NotExist_Fail() {
//        Given: Repository가 null 반환하도록 설정
        when(orderRepository.findByOrderIdAndUserId(userId, orderId)).thenReturn(null);

//        When: 실제 서비스 메서드 호출
        OrderDetailResV1 result = orderService.getOrderDetail(userId, orderId);

//        Then: null 반환 검증
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("주문 등록 - 성공")
    void createOrder_Success() {
//        Given: 테스트 입력 데이터와 Mock 동작 정의
//        빈 가짜 객체를 생성해서 모든 메서드 호출 시 기본값(null, 0. false) 반환
        StockEntity mockStockEntity = mock(StockEntity.class);
        OrderEntity mockOrderEntity = mock(OrderEntity.class);
//        Mock 동작 정의
//        Service 실행 중 savedOrder.getOrderId() 호출 시점에 1L 반환 - 안할 시 null 반환
        when(mockOrderEntity.getOrderId()).thenReturn(1L);

//        Repository Mock 동작 정의
//        Service 실행 중 stockRepository.findById() 호출 시점에 mockStockEntity 반환 - 안할 시 null 반환
        /* 각 when()은 Service 실행 중 특정 시점의 메서드 호출에 대한 가짜 응답
         * Service 로직이 중단되지 않고 끝까지 실행되도록 하는 환경 설정
         * 실제 DB나 복잡한 객체 생성 없이 Service 로직만 테스트 */
        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(mockOrderEntity); // save 메서드 호출 시 mockSavedOrder 객체 반환
        when(orderRepository.findByOrderIdAndUserId(userId, 1L)).thenReturn(mockOrderDetailResV1); // orderId, userId 호출 시 mockOrderDetailResDTO 반환

//        When: 실제 서비스 메서드 호출
        OrderDetailResV1 result = orderService.createOrder(userId, mockOrderReqV1);

//        Then: 비즈니스 로직 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.getOrderCount());
    }

    @Test
    @DisplayName("주문 등록 테스트 - 존재하지 않는 주식")
    void createOrder_NotExistStock_Fail() {
//        Given : 존재하지 않는 stockId로 설정
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(
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
                () -> {
                    orderService.createOrder(userId, mockOrderReqV1);
                });
    }

    @Test
    @DisplayName("주문 등록 테스트 - 주문 수량 0인 경우")
    void createOrder_insufficient_False() {
//        Given : 주문 수량 0인 데이터 생성
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(
                0,
                new BigDecimal("1500.00"),
                OrderType.SELL,
                10L,
                userId
        );

        StockEntity mockStockEntity = mock(StockEntity.class);
//        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));
//        수량검증을 이미 수행, OrderService > stockRepository 호출 X

//        When & Then : 유효성 검증 예외 발생
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    orderService.createOrder(userId, mockOrderReqV1);
                });
    }

    @Test
    @DisplayName("주문 등록 테스트 - 음수 가격 주문")
    void createOrder_NegativePrice_False() {
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(
                0,
                new BigDecimal("-1500.00"),
                OrderType.SELL,
                10L,
                userId
        );

        StockEntity mockStockEntity = mock(StockEntity.class);
//        when(stockRepository.findById(10L)).thenReturn(Optional.of(mockStockEntity));

//        When & Then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    orderService.createOrder(userId, mockOrderReqV1);
                });
    }

    @Test
    @DisplayName("주문 수정 테스트 - 성공")
    void updateOrder_Success() {
//        Given: 테스트 데이터 준비
//        1. 수정 전
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(
                20, // 기존 10 -> 20 으로 수정
                new BigDecimal("2000.00"), // 기존 1500 -> 2000 원으로 수정
                OrderType.BUY,
                10L,
                userId
        );

//        2. DB에서 조회된 기존 주문 entity(mock)
        OrderEntity mockOrderEntity = mock(OrderEntity.class);

//        3. Mock 동작 정의 => 기존 주문의 상태 설정(수정 가능 상태)
        when(mockOrderEntity.getUserId()).thenReturn(userId);
        when(mockOrderEntity.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(mockOrderEntity.getOrderExecutedCount()).thenReturn(0);
        when(mockOrderEntity.getOrderId()).thenReturn(orderId);

//        4. 수정 후 반환될 DTO(기대결과)
        OrderDetailResV1 expectResult = new OrderDetailResV1(
                10L, 1L, 20, new BigDecimal("2000.00"),
                OrderType.BUY, OrderStatus.PENDING, 20, 0,
                ZonedDateTime.now(), ZonedDateTime.now()
        );

//        5. Repository Mock 동작 정의
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));
        // "주문ID 로 주문을 찾았다"는 시나리오 가정
        when(orderRepository.findByOrderIdAndUserId(orderId, userId)).thenReturn(expectResult);
        // "수정 완료 후 최종 조회 결과"를 가정

//        When: 데이터 수정
        OrderDetailResV1 result = orderService.updateOrder(userId, orderId, mockOrderReqV1);

//        Then: 결과 검증
        Assertions.assertEquals(20, result.getOrderCount());
        Assertions.assertEquals(new BigDecimal("2000.00"), result.getOrderPrice());
    }

    @Test
    @DisplayName("존재하지 않는 주문 - 예외 발생")
    void updateOrder_OrderNotFound_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(20, new BigDecimal("1500.00"), OrderType.BUY, 10L, userId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(userId, orderId, mockOrderReqV1);
                });
        Assertions.assertEquals("수정할 주문을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("다른 사용자의 주문 수정 시도 - 예외 발생")
    void updateOrder_UnauthorizedUser_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(20, new BigDecimal("1500.00"), OrderType.BUY, 10L, 90L);

        OrderEntity mockOrderEntity = mock(OrderEntity.class);
        when(mockOrderEntity.getUserId()).thenReturn(userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(90L, orderId, mockOrderReqV1);
                });
        Assertions.assertEquals("수정 권한이 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 주문 수정 시도 - 예외 발생")
    void updateOrder_NotPendingStatus_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(20, new BigDecimal("1500.00"), OrderType.BUY, 10L, userId);

        OrderEntity mockOrderEntity = mock(OrderEntity.class);
        when(mockOrderEntity.getUserId()).thenReturn(userId);
        when(mockOrderEntity.getOrderStatus()).thenReturn(OrderStatus.COMPLETED); // PENDING 이 아님

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(userId, orderId, mockOrderReqV1);
                });

        Assertions.assertEquals("주문 상태는 PENDING만 수정 가능합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이미 체결된 주문 수정 시도 - 예외 발생")
    void updateOrder_AlreadyExecuted_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(20, new BigDecimal("1500.00"), OrderType.BUY, 10L, userId);

        OrderEntity mockOrderEntity = mock(OrderEntity.class);
        when(mockOrderEntity.getUserId()).thenReturn(userId);
        when(mockOrderEntity.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(mockOrderEntity.getOrderExecutedCount()).thenReturn(1);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(userId, orderId, mockOrderReqV1);
                });

        Assertions.assertEquals("이미 체결된 주문은 수정할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 수량이 0 이하 - 예외 발생")
    void updateOrder_InvalidCount_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(0, new BigDecimal("1500.00"), OrderType.BUY, 10L, userId);

        OrderEntity mockOrderEntity = mock(OrderEntity.class);
        when(mockOrderEntity.getUserId()).thenReturn(userId);
        when(mockOrderEntity.getOrderStatus()).thenReturn(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(userId, orderId, mockOrderReqV1);
                });
        Assertions.assertEquals("주문 수량은 1 이상 필수 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 가격이 0 이하 - 예외 발생")
    void updateOrder_InvalidPrice_ThrowsException() {
//        Given
        OrderReqV1 mockOrderReqV1 = new OrderReqV1(10, new BigDecimal("0"), OrderType.BUY, 10L, userId);

        OrderEntity mockOrderEntity = mock(OrderEntity.class);
        when(mockOrderEntity.getUserId()).thenReturn(userId);
        when(mockOrderEntity.getOrderStatus()).thenReturn(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrderEntity));

//        When & Then
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    orderService.updateOrder(userId, orderId, mockOrderReqV1);
                });
        Assertions.assertEquals("주문 가격은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void deleteOrder() {
    }
}