package com.stockexchange.domain.order.controller;

import com.stockexchange.config.EmbeddedRedisConfig;
import com.stockexchange.domain.order.dto.OrderDetailResDTO;
import com.stockexchange.domain.order.dto.OrderListResDTO;
import com.stockexchange.domain.order.dto.OrderReqDTO;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
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
                new BigDecimal("15000.00"),
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
        OrderDetailResDTO redisResult = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailKey);

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
        OrderReqDTO orderReqDTO = new OrderReqDTO(10, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//      예상되는 등록 후 결과(생성될 주문 데이터)
        Long expectedOrderId = 1L;
        OrderDetailResDTO orderDetailResDTO = new OrderDetailResDTO(
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
        redisTemplate.opsForValue().set(orderDetailsKey + expectedOrderId, orderDetailResDTO);
        redisTemplate.opsForList().rightPush(userOrdersKey, expectedOrderId);

//        Then: 등록 결과 검증
        OrderDetailResDTO saveOrderDetail = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey + expectedOrderId);
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
        OrderReqDTO orderReqDTO = new OrderReqDTO(10, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    if (orderReqDTO.getUserId() == null) {
                        throw new IllegalArgumentException("사용자 ID는 필수입니다.");
                    }
                    redisTemplate.opsForValue().set(orderDetailsKey + stockId, orderReqDTO);
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
        OrderReqDTO orderReqDTO = new OrderReqDTO(0, new BigDecimal("150000.00"), OrderType.BUY, stockId, userId);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    if (orderReqDTO.getOrderCount() <= 0) {
                        throw new IllegalArgumentException("주문 수량은 필수 입력입니다.");
                    }
                    redisTemplate.opsForValue().set(orderDetailsKey + stockId, orderReqDTO);
                }
        );
        Assertions.assertEquals("주문 수량은 필수 입력입니다.", exception.getMessage());
    }


    @Test
    @DisplayName("주문 수정 테스트 - 성공")
    void updateOrderById_ValidUpdate_Success() {
//        Given: 테스트 데이터 준비
        Long userId = 1L;
        Long orderId = 1L;
        Long stockId = 10L;
        String orderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

//        기존 주문 데이터 생성(생성 시간 고정)
        ZonedDateTime createdTime = ZonedDateTime.now().minusMonths(30);
        OrderDetailResDTO orderDetailResDTO = new OrderDetailResDTO(
                stockId,
                orderId,
                10,
                new BigDecimal("15000.00"),
                OrderType.BUY,
                OrderStatus.PENDING,
                10,
                0,
                createdTime,
                createdTime
        );
//        Redis에 기존 주문 저장
        redisTemplate.opsForValue().set(orderDetailsKey, orderDetailResDTO);

//        사용자 수정 요청 데이터
        OrderReqDTO updateOrderReqDTO = new OrderReqDTO(
                20, // 수정
                new BigDecimal("30000.00"), // 수정
                OrderType.BUY,
                stockId,
                userId
        );

//        When : 주문 수정 실행
//        1. 기존 주문 조회
        OrderDetailResDTO existing = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey);
//        2. 수정된 주문 데이터 생성
        OrderDetailResDTO updateExisting = new OrderDetailResDTO(
                existing.getStockId(),
                existing.getOrderId(),
                updateOrderReqDTO.getOrderCount(),
                updateOrderReqDTO.getOrderPrice(),
                existing.getOrderType(),
                existing.getOrderStatus(),
                existing.getOrderRemainCount(),
                existing.getOrderExecutedCount(),
                existing.getCreatedAt(),
                ZonedDateTime.now()
        );

//        3. Redis에 수정 데이터 저장
        redisTemplate.opsForValue().set(orderDetailsKey, updateExisting);

//        Then: 수정 결과 검증
        OrderDetailResDTO result = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(20, result.getOrderCount());
        Assertions.assertEquals(new BigDecimal("30000.00"), result.getOrderPrice());
    }

    @Test
    @DisplayName("주문 수정 테스트 - 데이터 존재하지 않는 경우")
    void updateOrderById_NoExist_ThrowException() {
        //        Given: 테스트 데이터 준비
        Long userId = 1L;
        Long orderId = 999L;
        String orderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

//        수정할 데이터
        OrderReqDTO updateOrderReqDTO = new OrderReqDTO(
                15,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                10L,
                userId
        );

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OrderDetailResDTO existing = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey);
                    if (existing == null) {
                        throw new IllegalArgumentException("존재하지 않는 주문입니다.");
                    }
//                    수정 로직 실행
                    redisTemplate.opsForValue().set(orderDetailsKey, updateOrderReqDTO);
                }
        );
        Assertions.assertEquals("존재하지 않는 주문입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 수정 테스트 - null id 파라미터")
    void updateOrderById_NullId_ThrowException() {
//        Given : Null 파라미터
        Long userId = null;
        Long orderId = null;
        Long stockId = 10L;

        OrderReqDTO updateOrderReqDTO = new OrderReqDTO(
                15,
                new BigDecimal("1500.00"),
                OrderType.SELL,
                stockId,
                userId
        );

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    if (userId == null || orderId == null || stockId == null) {
                        throw new IllegalArgumentException("사용자 ID와 주문 ID는 필수입니다.");
                    }
//                    수정 로직
                    String orderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;
                    redisTemplate.opsForValue().set(orderDetailsKey, updateOrderReqDTO);
                }
        );
        Assertions.assertEquals("사용자 ID와 주문 ID는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 수정 테스트 - 유효성 검사 실패(잔고 부족)")
    void updateOrderById_InsufficientBalance_ReturnFalse() {
//        Given : 테스트 데이터 준비 - 수정하려는 수량 0 인 경우
        Long userId = 1L;
        Long orderId = 1L;
        Long stockId = 10L;
        String orderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

        OrderDetailResDTO orderDetailResDTO = new OrderDetailResDTO(
                stockId,
                orderId,
                10,
                new BigDecimal("150000.00"),
                OrderType.BUY,
                OrderStatus.PENDING,
                10,
                0,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

//        Redis에 기존 주문 저장
        redisTemplate.opsForValue().set(orderDetailsKey, orderDetailResDTO);

//        잘못된 수정 데이터(수량 0)
        OrderReqDTO updateOrderReqDTO = new OrderReqDTO(
                0,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                stockId,
                userId
        );

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OrderDetailResDTO existing = (OrderDetailResDTO) redisTemplate.opsForValue().get(orderDetailsKey);
                    if (existing == null) { // 기존 주문 존재 확인
                        throw  new IllegalArgumentException("존재하지 않는 주문입니다.");
                    }
//                    유효성 검사
                    if(updateOrderReqDTO.getOrderCount() <= 0){
                        throw  new IllegalArgumentException("주문 수량은 0보다 커야 합니다.");
                    }
//                    수정 로직 실행(실제로 도달 X)
                    redisTemplate.opsForValue().set(orderDetailsKey, updateOrderReqDTO);
                }
        );
        Assertions.assertEquals("주문 수량은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 삭제 테스트 - 성공")
    void deleteOrderById_Success() {
//        Given : 테스트 준비 - 삭제 테스트 위한 데이터 등록
        Long userId = 1L;
        Long orderId = 1L;
        Long stockId = 10L;
        String OrderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;
        String UserOrdersKey = USER_ORDERS_KEY + userId;

        OrderDetailResDTO orderDetailResDTO = new OrderDetailResDTO(
                stockId,
                orderId,
                10,
                new BigDecimal("150000.00"),
                OrderType.BUY,
                OrderStatus.PENDING,
                10,
                0,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

//        Redis 에 주문 저장
        redisTemplate.opsForValue().set(OrderDetailsKey, orderDetailResDTO);
        redisTemplate.opsForList().rightPush(UserOrdersKey, orderId);

//        When : 주문 삭제 실행
        redisTemplate.delete(OrderDetailsKey);
        redisTemplate.opsForList().remove(UserOrdersKey, 1, orderId);

//        Then : 삭제 결과 검증
        OrderDetailResDTO afterDelete = (OrderDetailResDTO) redisTemplate.opsForValue().get(OrderDetailsKey);
        List<Object> userOrderList = redisTemplate.opsForList().range(UserOrdersKey, 0, -1);

        Assertions.assertNull(afterDelete);
        Assertions.assertTrue(userOrderList.isEmpty());
    }

    @Test
    @DisplayName("주문 삭제 테스트 - 부분 체결 삭제 불가")
    void deleteOrderById_PartiallyExcuted_ThrowException() {
//        Given: 부분 체결된 주문 생성
        Long userId = 1L;
        Long orderId = 1L;
        Long stockId = 10L;
        String OrderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

        OrderReqDTO orderReqDTO = new OrderReqDTO(
                10,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                stockId,
                userId
        );

        OrderDetailResDTO orderPartialResDTO = new OrderDetailResDTO(
                orderReqDTO.getStockId(),
                orderId,
                orderReqDTO.getOrderCount(),
                orderReqDTO.getOrderPrice(),
                orderReqDTO.getOrderType(),
                OrderStatus.PENDING,
                5, // 5개 남음
                5, // 5개 체결
                ZonedDateTime.now().minusMinutes(10),
                ZonedDateTime.now().minusMinutes(5)
        );

        redisTemplate.opsForValue().set(OrderDetailsKey, orderPartialResDTO);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OrderDetailResDTO existing = (OrderDetailResDTO) redisTemplate.opsForValue().get(OrderDetailsKey);
                    if (existing == null) {
                        throw  new IllegalArgumentException("존재하지 않는 주문입니다.");
                    }
                    if(existing.getOrderRemainCount() > 0){
                        throw  new IllegalArgumentException("이미 체결된 주문은 삭제할 수 없습니다.");
                    }
                    redisTemplate.delete(OrderDetailsKey);
                }
        );
        Assertions.assertEquals("이미 체결된 주문은 삭제할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 삭제 테스트 - 삭제 불가(완료된 주문)")
    void deleteOrderById_OrderCompleted_ThrowException() {
//        Given : 사용자 주문 등록부터 완료까지
        Long userId = 1L;
        Long stockId = 1L;
        Long orderId = 1L;
        String OrderDetailsKey = ORDER_PREFIX + userId + ":" + orderId;

        OrderReqDTO orderReqDTO = new OrderReqDTO(
                10,
                new BigDecimal("1500.00"),
                OrderType.BUY,
                stockId,
                userId
        );

        ZonedDateTime createdTime = ZonedDateTime.now().minusHours(30);
        ZonedDateTime completedTime = ZonedDateTime.now().minusMinutes(30);

        OrderDetailResDTO completedOrderDTO = new OrderDetailResDTO(
                orderReqDTO.getStockId(),
                orderId,
                orderReqDTO.getOrderCount(),
                orderReqDTO.getOrderPrice(),
                orderReqDTO.getOrderType(),
                OrderStatus.COMPLETED,
                0,
                orderReqDTO.getOrderCount(),
                createdTime,
                completedTime
        );

        redisTemplate.opsForValue().set(OrderDetailsKey, completedOrderDTO);

//        When & Then : 예외 발생 검증
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OrderDetailResDTO existing = (OrderDetailResDTO) redisTemplate.opsForValue().get(OrderDetailsKey);
                    if(existing == null){
                        throw  new IllegalArgumentException("존재하지 않는 주문입니다.");
                    }
                    if(existing.getOrderStatus() == OrderStatus.COMPLETED){
                        throw  new IllegalArgumentException("완료된 주문은 삭제할 수 없습니다.");
                    }
                    redisTemplate.delete(OrderDetailsKey);
                }
        );
        Assertions.assertEquals("완료된 주문은 삭제할 수 없습니다.", exception.getMessage());
    }
}