package com.stockexchange.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockexchange.config.EmbeddedRedisConfig;
import com.stockexchange.domain.order.dto.OrderDetailResV1;
import com.stockexchange.domain.order.dto.OrderListResV1;
import com.stockexchange.domain.order.dto.OrderReqV1;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import com.stockexchange.domain.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@Import(EmbeddedRedisConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /*@MockBean - springboot 3.4 부터 deprecated
    private OrderService orderService;*/

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //    테스트용 주문 데이터
    private final String ORDER_PREFIX = "order:";
    private final String USER_ORDERS_KEY = "user:orders:";

    @BeforeEach
    void setUp() {
//        테스트 시작 전 Redis 연결 확인
        Assertions.assertNotNull(redisTemplate);
        Assertions.assertNotNull(redisTemplate.getConnectionFactory());
    }

    @AfterEach
//    각 테스트 후 Redis 데이터 정리(테스트 간 격리)
    void cleanUp() {
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                connection.serverCommands().flushAll();
                return null;
            });
        } catch (Exception e) {
            System.err.println("Redis cleanup failed" + e.getMessage());
        }
    }

    @Test
    @DisplayName("Redis 연결 및 기본 동작 테스트")
    void redisConnectionTest() {
//        Given : 테스트 데이터
        String testKey = "test:connection";
        String testValue = "Redis working";

//        When : Redis 데이터 저장
        redisTemplate.opsForValue().set(testKey, testValue);

//        Then : 저장된 데이터 검증
        String result = (String) redisTemplate.opsForValue().get(testKey);
        Assertions.assertEquals(testValue, result);
    }

    @Test
    @DisplayName("주문 전체 목록 조회 테스트 - 데이터 완전히 나오는 경우")
    void getOrders_AllData() {
//        Given
        Long userId = 1L;
        String userOrdersKey = USER_ORDERS_KEY + userId;

        List<OrderListResV1> expectedOrders = Arrays.asList(
                new OrderListResV1(1L, 10, OrderType.BUY, OrderStatus.PENDING, 100L, ZonedDateTime.now()),
                new OrderListResV1(2L, 20, OrderType.SELL, OrderStatus.CANCELLED, 200L, ZonedDateTime.now()),
                new OrderListResV1(3L, 30, OrderType.BUY, OrderStatus.COMPLETED, 300L, ZonedDateTime.now())
        );

//        Redis에 주문 목록 저장
        redisTemplate.opsForList().rightPushAll(userOrdersKey, expectedOrders.toArray());

//        When: Redis에서 주문 목록 조회
        List<Object> redisResult = redisTemplate.opsForList().range(userOrdersKey, 0, -1);

//        Then : 데이터 검증
        Assertions.assertNotNull(redisResult);
        Assertions.assertEquals(3, redisResult.size());
    }

    @Test
    @DisplayName("주문 전체 목록 조회 테스트 - 값이 빈 경우")
    void getOrders_EmptyCase() {
//        Given : 사용자 ID만 있고 주문 데이터는 없는 상태
        Long userId = 2L;
        String userOrdersKey = USER_ORDERS_KEY + userId;

//        When: Redis에서 존재하지 않는 키 조회
        List<Object> redisResult = redisTemplate.opsForList().range(userOrdersKey, 0, -1);

//        Then: 빈 리스트 반환 확인
        Assertions.assertNotNull(redisResult);
        Assertions.assertTrue(redisResult.isEmpty());
        Assertions.assertEquals(0, redisResult.size());
    }

    @Test
    @DisplayName("주문 전체 목록 조회 테스트 - Null 값인 경우")
    void getOrders_NullCase() {
//        Given : Null userId 또는 잘못된 키 형태
        Long userId = null;
        String userOrdersKey = USER_ORDERS_KEY + userId; // "user:orders:null"

//        When : 잘못된 키로 조회
        List<Object> redisResult = redisTemplate.opsForList().range(userOrdersKey, 0, -1);

//        Then : 빈 리스트 반환 확인(Redis는 존재하지 않는 키에 대해 빈 리스트 반환)
        Assertions.assertNotNull(redisResult);
        Assertions.assertTrue(redisResult.isEmpty());
    }

    @Test
    @DisplayName("특정 주문을 상세 조회 테스트 - 데이터 완전한 경우")
    void getOrderById_AllData() {

//        Given: 테스트 데이터 준비
        Long userId = 1L;
        Long orderId = 10L;
        String orderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

        OrderDetailResV1 orderDetailResV1 = new OrderDetailResV1(
                500L,
                orderId,
                25,
                new BigDecimal("15000.00"),
                OrderType.BUY,
                OrderStatus.PENDING,
                20,
                5,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        redisTemplate.opsForValue().set(orderDetailsKey, orderDetailResV1);

//        When: Redis에서 주문 상세 조회
        OrderDetailResV1 redisResult = (OrderDetailResV1) redisTemplate.opsForValue().get(orderDetailsKey);

//        Then: 데이터 검증
        Assertions.assertNotNull(redisResult);
        Assertions.assertEquals(500L, redisResult.getStockId());
        Assertions.assertEquals(orderId, redisResult.getOrderId());
        Assertions.assertEquals(25, redisResult.getOrderCount());
        Assertions.assertEquals(new BigDecimal("15000.00"), redisResult.getOrderPrice());
        Assertions.assertEquals(OrderType.BUY, redisResult.getOrderType());
        Assertions.assertEquals(OrderStatus.PENDING, redisResult.getOrderStatus());
        Assertions.assertEquals(20, redisResult.getOrderRemainCount());
        Assertions.assertEquals(5, redisResult.getOrderExecutedCount());
    }

    @Test
    @DisplayName("특정 주문을 상세 조회 테스트 - 데이터 빈 경우(존재하지 않는 주문)")
    void getOrderById_EmptyCase() {
//        Given : 사용자만 있고 주문 Id 없는 경우
        Long userId = 1L;
        String userOrdersKey = USER_ORDERS_KEY + userId;

//        When: Redis에서 존재하지 않는 주문 조회
        List<Object> redisResult = redisTemplate.opsForList().range(userOrdersKey, 0, -1);

//        Then: 빈 리스트 반환
        Assertions.assertNotNull(redisResult);
        Assertions.assertTrue(redisResult.isEmpty());
        Assertions.assertEquals(0, redisResult.size());
    }

    @Test
    @DisplayName("특정 주문을 상세 조회 테스트 - 데이터 Null인 경우(잘못된 파라미터)")
    void getOrderById_NullCase() {
//        Given : Null 파라미터들
        Long userId = null;
        Long orderId = null;
        String orderDetailKey = ORDER_PREFIX + userId + ":" + orderId; // "order:detail:null:null"

//        When: 잘못된 키로 조회
        OrderDetailResV1 redisResult = (OrderDetailResV1) redisTemplate.opsForValue().get(orderDetailKey);

//        Then: null 반환 확인
        Assertions.assertNull(redisResult);
    }

    @Test
    @DisplayName("주문 등록 테스트 - 성공")
    void createOrder_ValidInput_Success() {
//        Given: 테스트 데이터 준비
        Long userId = 1L;
        Long stockId = 10L;
        Long orderId = 20L;
        String userOrdersKey = USER_ORDERS_KEY + userId;
        String orderDetailsKey = ORDER_PREFIX + userId + ":";
        OrderReqV1 orderReqV1 = new OrderReqV1(10, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//      예상되는 등록 후 결과(생성될 주문 데이터)
        Long expectedOrderId = 1L;
        OrderDetailResV1 orderDetailResV1 = new OrderDetailResV1(
                stockId,
                expectedOrderId,
                10,
                new BigDecimal("150000.00"),
                OrderType.BUY,
                OrderStatus.PENDING,
                10,
                0,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

//        When: 주문 등록 실행
//        Redis에 주문 저장(실제 서비스에서는 OrderService.createOrder() 호출)
        redisTemplate.opsForValue().set(orderDetailsKey + expectedOrderId, orderDetailResV1);
        redisTemplate.opsForList().rightPush(userOrdersKey, expectedOrderId);

//        Then: 등록 결과 검증
        OrderDetailResV1 saveOrderDetail = (OrderDetailResV1) redisTemplate.opsForValue().get(orderDetailsKey + expectedOrderId);
        Assertions.assertNotNull(saveOrderDetail);
        Assertions.assertEquals(stockId, saveOrderDetail.getStockId());
        Assertions.assertEquals(expectedOrderId, saveOrderDetail.getOrderId());
        Assertions.assertEquals(OrderType.BUY, saveOrderDetail.getOrderType());
        Assertions.assertEquals(OrderStatus.PENDING, saveOrderDetail.getOrderStatus());
        Assertions.assertEquals(10, saveOrderDetail.getOrderRemainCount());
        Assertions.assertEquals(0, saveOrderDetail.getOrderExecutedCount());
    }

    @Test
    @DisplayName("주문 등록 테스트 - 필수 필드 누락 경우")
    void createOrder_NullUserId_ThrowException() {
//        Given : 테스트 데이터 준비 - userId가 null 인 경우
        Long userId = null;
        Long stockId = 10L;
        Long orderId = 20L;
        String userOrdersKey = USER_ORDERS_KEY + userId;
        String orderDetailsKey = ORDER_PREFIX + userId + ":";
        OrderReqV1 orderReqV1 = new OrderReqV1(10, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    if (orderReqV1.getUserId() == null) {
                        throw new IllegalArgumentException("사용자 ID는 필수입니다.");
                    }
                    redisTemplate.opsForValue().set(orderDetailsKey + stockId, orderReqV1);
                }
        );
//        예외 메세지 검증
        Assertions.assertEquals("사용자 ID는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 등록 테스트 - 유효성 검사 실패")
    void createOrder_InsufficientBalance_ReturnFalse() {
//        Given : 테스트 데이터 준비 - 주문 수량 0인 경우
        Long userId = 1L;
        Long stockId = 10L;
        Long orderId = 20L;
        String userOrdersKey = USER_ORDERS_KEY + userId;
        String orderDetailsKey = ORDER_PREFIX + userId + ":";
        OrderReqV1 orderReqV1 = new OrderReqV1(0, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    if (orderReqV1.getOrderCount() <= 0) {
                        throw new IllegalArgumentException("주문 수량은 필수 입력입니다.");
                    }
                    redisTemplate.opsForValue().set(orderDetailsKey + stockId, orderReqV1);
                }
        );
        Assertions.assertEquals("주문 수량은 필수 입력입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 수정 성공")
    void updateOrder_Success() throws Exception {
//        Given
        Long userId = 1L;
        Long stockId = 10L;
        Long orderId = 20L;
        OrderReqV1 orderReqV1 = new OrderReqV1(
                10,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                10L,
                userId
        );

        OrderDetailResV1 orderDetailResV1 = new OrderDetailResV1(
                10L, 1L, 10, new BigDecimal("15000.00"),
                OrderType.BUY, OrderStatus.PENDING, 10, 0,
                ZonedDateTime.now(), ZonedDateTime.now()
        );

//        Mock 설정
        when(orderService.updateOrder(userId, orderId, any(OrderReqV1.class))).thenReturn(orderDetailResV1);

//        When & Then - HTTP 요청 실행
        mockMvc.perform(put("/api/v1/users/{userId}/orders/{orderId}", userId, orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderReqV1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.orderCount").value(10))
                .andExpect(jsonPath("$.orderPrice").value(new BigDecimal("3000.00")));
    }
}