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