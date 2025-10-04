package com.stockexchange.domain.order.repository;

import com.stockexchange.config.EmbeddedRedisConfig;
import com.stockexchange.domain.order.entity.OrderEntity;
import com.stockexchange.domain.order.entity.OrderStatus;
import com.stockexchange.domain.order.entity.OrderType;
import com.stockexchange.domain.stock.entity.StockEntity;
import com.stockexchange.domain.stock.entity.StockIpo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/* OrderRepository JPA 테스트 클래스
 * @DataJpaTest 를 사용해서 JPA Repository 계층만 테스트
 * JPA 관련 빈만 로드합니다 - Repository, Entity,, 따라서 Service, Controller은 로드 X */
@DataJpaTest
@Import(EmbeddedRedisConfig.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private StockEntity testStock;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
//        테스트용 주식 엔티티 생성 및 저장
        testStock = new StockEntity(
                "테스트 주식 종목",
                "TEST001",
                StockIpo.KOSDAQ,
                new BigDecimal("1500.00")
        );
        testStock = entityManager.persistAndFlush(testStock);
    }

    @Test
    @Order(1)
    @DisplayName("JPA Repository 기본 동작 테스트")
    void repositoryBasicOperationTest() {
//        Given: 테스트용 주문 엔터티
        OrderEntity orderEntity = createTestOrder(10, new BigDecimal("1500.00"), OrderType.BUY);

//        When: 주문 저장
        OrderEntity savedOrder = orderRepository.save(orderEntity);

//        Then: 저장 검증
        Assertions.assertNotNull(savedOrder);
        Assertions.assertEquals(userId, savedOrder.getUserId());
    }

    @Test
    @Order(2)
    @DisplayName("주문 목록 조회 테스트 - 성공")
    void getOrders_AllData() {
//        Given : 주문 엔티티 생성
        OrderEntity order1 = createTestOrder(10, new BigDecimal("1500.00"), OrderType.BUY);
        OrderEntity order2 = createTestOrder(20, new BigDecimal("3000.00"), OrderType.SELL);
        OrderEntity order3 = createTestOrder(15, new BigDecimal("50000.00"), OrderType.BUY);

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        entityManager.flush();

//        When: 사용자별 주문 목록 조회
        List<OrderEntity> result = orderRepository.findAllByUserId(userId);

//        Then: 주문 조회 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());
    }

    @Test
    @Order(3)
    @DisplayName("주문 목록 조회 테스트 - 데이터 없는 경우")
    void getOrders_EmptyCase() {
//        Given: 사용자 ID 만 있고 주문 데이터는 없는 상태
        Long notExistOrderId = 999L;

//        When: 존재하지 않는 주문 조회
        OrderEntity result = orderRepository.findByOrderIdAndUserId(notExistOrderId, userId);

//        Then: null 값 반환
        Assertions.assertNull(orderRepository.findByOrderIdAndUserId(notExistOrderId, userId));
        Assertions.assertNull(result);
    }

    @Test
    @Order(4)
    @DisplayName("주문 목록 조회 테스트 - 특정 데이터 조회")
    void getOrders_partCase() {
//        Given: 주문 엔티티 생성
        OrderEntity order1 = createTestOrderDetail(
                10, new BigDecimal("1500.00"), OrderType.BUY, 20, 5);
        OrderEntity result = orderRepository.save(order1);
        entityManager.flush();

//      When: 주문 상세 조회
        OrderEntity orderDetailResV1 = orderRepository.findByOrderIdAndUserId(result.getOrderId(), userId);

//        Then : 조회 결과 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(orderDetailResV1.getOrderId(), result.getOrderId());
    }

    @Test
    @Order(5)
    @DisplayName("주문 목록 조회 테스트 - 특정 데이터 존재하지 않는 경우")
    void getOrders_NotExist_ReturnNull() {
//        Given: 존재하지 않는 주문 id
        Long notExistOrderId = 999L;

//        When: 존재하지 않는 주문 조회
        OrderEntity result = orderRepository.findByOrderIdAndUserId(notExistOrderId, userId);

//        Then: Null 값 반환
        Assertions.assertNull(result);
    }

    @Test
    @Order(6)
    @DisplayName("주문 등록 테스트 - 성공")
    void createOrder_Success() {
//        Given : 테스트 데이터 준비
        OrderEntity order1 = createTestOrder(10, new BigDecimal("1500.00"), OrderType.BUY);

//        When : 주문 등록 테스트
        OrderEntity result = orderRepository.save(order1);
        entityManager.flush();

//        Then: 검증
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.getOrderCount());


    }

    @Test
    @Order(7)
    @DisplayName("주문 등록 테스트 - 필수 필드 누락")
    void createOrder_NullUserId_False() {
//        Given: 등록 데이터 준비 - 주문 타입 null
        OrderEntity order1 = createTestOrder(5, new BigDecimal("1500.00"), null);

//        When & Then : 예외 발생 검증
        Assertions.assertThrows(Exception.class,
                () -> {
                    orderRepository.save(order1);
                    entityManager.flush();
                });
    }

    @Test
    @Order(8)
    @DisplayName("주문 등록 테스트 - 주문 수량 0")
    void createOrder_insufficient_False() {
//        Given : 등록 데이터 준비
        OrderEntity order1 = createTestOrder(0, new BigDecimal("1500.00"), OrderType.BUY);

//        When & Then : 예외 발생 검증
        Assertions.assertThrows(Exception.class,
                () -> {
                    orderRepository.save(order1);
                    entityManager.flush();
                });
    }


    /*    테스트용 주문 엔티티 생성 헬퍼 메서드
        repository는 Entity와 직접 상호 작용하는 계층 */
    private OrderEntity createTestOrder(int orderCount, BigDecimal orderPrice, OrderType orderType) {
        return new OrderEntity(
                orderCount,
                orderPrice,
                orderType,
                OrderStatus.PENDING,
                orderCount, //orderCount = orderRemainCount
                0,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                testStock,
                userId
        );
    }

    private OrderEntity createTestOrderDetail(int orderCount, BigDecimal orderPrice, OrderType orderType, int orderRemainCount, int orderExecutedCount) {
        return new OrderEntity(
                orderCount,
                orderPrice,
                orderType,
                OrderStatus.PENDING,
                orderRemainCount,
                orderExecutedCount,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                testStock,
                userId
        );
    }
}