package com.stockexchange.domain.order.controller;

import com.stockexchange.config.EmbeddedRedisConfig;
import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Import(EmbeddedRedisConfig.class)
class OrderControllerTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //    테스트용 주문 데이터
    private final String ORDER_PREFIX = "order:";
    private final String USER_ORDERS_KEY = "user:orders:";
    private final String ALL_ORDERS_KEY = "orders:detail:";

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

        List<OrderListResDTO> expectedOrders = Arrays.asList(
                new OrderListResDTO(1L, 10, OrderType.BUY, OrderStatus.PENDING, 100L, ZonedDateTime.now()),
                new OrderListResDTO(2L, 20, OrderType.SELL, OrderStatus.CANCELLED, 200L, ZonedDateTime.now()),
                new OrderListResDTO(3L, 30, OrderType.BUY, OrderStatus.COMPLETED, 300L, ZonedDateTime.now())
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

        OrderDetailResDTO orderDetailResDTO = new OrderDetailResDTO(
                500L,
                orderId,
                25,
                15000L,
                OrderType.BUY,
                OrderStatus.PENDING,
                20,
                5,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        redisTemplate.opsForValue().set(orderDetailsKey, orderDetailResDTO);

//        When: Redis에서 주문 상세 조회
        OrderDetailResDTO redisResult = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey);

//        Then: 데이터 검증
        Assertions.assertNotNull(redisResult);
        Assertions.assertEquals(500L, redisResult.getStockId());
        Assertions.assertEquals(orderId, redisResult.getOrderId());
        Assertions.assertEquals(25, redisResult.getOrderCount());
        Assertions.assertEquals(15000L, redisResult.getOrderPrice());
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
        OrderDetailResDTO redisResult = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailKey);

//        Then: null 반환 확인
        Assertions.assertNull(redisResult);
    }

    @Test
    @Disabled
    void createOrder() {
        //        TODO
        throw new NotImplementedException();
    }

    @Test
    @Disabled
    void updateOrderById() {
        //        TODO
        throw new NotImplementedException();
    }

    @Test
    @Disabled
    void deleteOrderById() {
        //        TODO
        throw new NotImplementedException();
    }
}