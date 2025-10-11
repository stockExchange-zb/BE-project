USE stock;

-- 외래키 체크 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 데이터 삭제
TRUNCATE TABLE holding;
TRUNCATE TABLE execution;
TRUNCATE TABLE order_table;
TRUNCATE TABLE stock;
TRUNCATE TABLE users;

-- 외래키 체크 활성화
SET FOREIGN_KEY_CHECKS = 1;

INSERT IGNORE INTO users (user_name, user_krw_price, created_at) VALUES
('김투자', 10000000.00, NOW()),
('박거래', 5000000.00, NOW()),
('이주식', 15000000.00, NOW()),
('최매수', 8000000.00, NOW()),
('한매도', 12000000.00, NOW());

INSERT IGNORE INTO stock (stock_name, stock_number, stock_ipo, stock_price) VALUES
('삼성전자', '005930', 'KOSPI', 71000.00),
('SK하이닉스', '000660', 'KOSPI', 89500.00),
('NAVER', '035420', 'KOSPI', 191000.00),
('카카오', '035720', 'KOSPI', 45500.00),
('LG에너지솔루션', '373220', 'KOSPI', 435000.00);

INSERT IGNORE INTO order_table (order_count, order_price, order_type, order_status, order_remain_count, order_executed_count, created_at, updated_at, stock_id, user_id) VALUES
(10, 71000.00, 'BUY', 'COMPLETED', 0, 10, NOW(), NOW(), 1, 1),
(5, 89500.00, 'BUY', 'COMPLETED', 0, 5, NOW(), NOW(), 2, 1),
(20, 191000.00, 'BUY', 'PENDING', 20, 0, NOW(), NOW(), 3, 2);

INSERT IGNORE INTO holding (holding_quantity, holding_total_price, updated_at, user_id, stock_id) VALUES
(10, 710000.00, NOW(), 1, 1),
(5, 447500.00, NOW(), 1, 2),
(30, 1365000.00, NOW(), 2, 4);

-- 기존 INSERT 구문들 이후에 추가

-- 삼성전자(stock_id=1) 매수/매도 주문들
INSERT IGNORE INTO order_table (order_count, order_price, order_type, order_status, order_remain_count, order_executed_count, created_at, updated_at, stock_id, user_id) VALUES
-- 매수 주문들
(10, 71000.00, 'BUY', 'PENDING', 10, 0, NOW(), NOW(), 1, 2),  -- 삼성전자 매수 71,000원
(15, 70500.00, 'BUY', 'PENDING', 15, 0, NOW(), NOW(), 1, 3),  -- 삼성전자 매수 70,500원
(20, 71500.00, 'BUY', 'PENDING', 20, 0, NOW(), NOW(), 1, 4),  -- 삼성전자 매수 71,500원 (높은가격)

-- 매도 주문들
(10, 71000.00, 'SELL', 'PENDING', 10, 0, NOW(), NOW(), 1, 5),  -- 삼성전자 매도 71,000원 (매칭 가능!)
(8, 70800.00, 'SELL', 'PENDING', 8, 0, NOW(), NOW(), 1, 3),   -- 삼성전자 매도 70,800원 (매칭 가능!)
(12, 72000.00, 'SELL', 'PENDING', 12, 0, NOW(), NOW(), 1, 2);  -- 삼성전자 매도 72,000원

-- SK하이닉스(stock_id=2) 매수/매도 주문들
INSERT IGNORE INTO order_table (order_count, order_price, order_type, order_status, order_remain_count, order_executed_count, created_at, updated_at, stock_id, user_id) VALUES
-- 매수 주문들
(5, 89500.00, 'BUY', 'PENDING', 5, 0, NOW(), NOW(), 2, 3),   -- SK하이닉스 매수
(10, 90000.00, 'BUY', 'PENDING', 10, 0, NOW(), NOW(), 2, 4),  -- SK하이닉스 매수 (높은가격)

-- 매도 주문들
(5, 89500.00, 'SELL', 'PENDING', 5, 0, NOW(), NOW(), 2, 2),  -- SK하이닉스 매도 (매칭 가능!)
(7, 89000.00, 'SELL', 'PENDING', 7, 0, NOW(), NOW(), 2, 5);  -- SK하이닉스 매도 (낮은가격, 매칭 가능!)

-- NAVER(stock_id=3) 매수/매도 주문들
INSERT IGNORE INTO order_table (order_count, order_price, order_type, order_status, order_remain_count, order_executed_count, created_at, updated_at, stock_id, user_id) VALUES
-- 매수 주문들
(3, 191000.00, 'BUY', 'PENDING', 3, 0, NOW(), NOW(), 3, 4),
(5, 192000.00, 'BUY', 'PENDING', 5, 0, NOW(), NOW(), 3, 5),

-- 매도 주문들
(3, 191000.00, 'SELL', 'PENDING', 3, 0, NOW(), NOW(), 3, 1),  -- 매칭 가능!
(4, 190000.00, 'SELL', 'PENDING', 4, 0, NOW(), NOW(), 3, 2);  -- 낮은가격, 매칭 가능!

-- 카카오(stock_id=4) 매수/매도 주문들
INSERT IGNORE INTO order_table (order_count, order_price, order_type, order_status, order_remain_count, order_executed_count, created_at, updated_at, stock_id, user_id) VALUES
-- 매수 주문들
(25, 45500.00, 'BUY', 'PENDING', 25, 0, NOW(), NOW(), 4, 1),
(30, 46000.00, 'BUY', 'PENDING', 30, 0, NOW(), NOW(), 4, 3),

-- 매도 주문들
(20, 45500.00, 'SELL', 'PENDING', 20, 0, NOW(), NOW(), 4, 4),  -- 매칭 가능!
(15, 45000.00, 'SELL', 'PENDING', 15, 0, NOW(), NOW(), 4, 5);  -- 낮은가격, 매칭 가능!