-- CREATE DATABASE stock;

USE stock;

-- 기존 테이블 삭제
DROP TABLE IF EXISTS holding;
DROP TABLE IF EXISTS execution;
DROP TABLE IF EXISTS order_table;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS user;

-- 회원 테이블
CREATE TABLE IF NOT EXISTS user (
    user_id bigint NOT NULL AUTO_INCREMENT COMMENT '회원 아이디',
    user_name varchar(20) NOT NULL COMMENT '회원명',
    user_krw_price bigint NOT NULL COMMENT '보유 원화 현금',
    created_at timestamp NOT NULL COMMENT '가입 시간',
    PRIMARY KEY (user_id)
) ENGINE=INNODB COMMENT='회원';

-- 주식 종목 테이블
CREATE TABLE IF NOT EXISTS stock (
    stock_id bigint NOT NULL AUTO_INCREMENT COMMENT '주식 아이디',
    stock_name varchar(255) NOT NULL COMMENT '종목명',
    stock_number int NOT NULL COMMENT '종목 코드',
    stock_ipo enum('KOSPI','KOSDAQ') NOT NULL COMMENT '상장시장',
    stock_price int NOT NULL COMMENT '종목 가격',
    PRIMARY KEY (stock_id)
) ENGINE=INNODB COMMENT='주식 종목';

-- 주문 테이블 
CREATE TABLE IF NOT EXISTS order_table (
    order_id bigint NOT NULL AUTO_INCREMENT COMMENT '주문 아이디',
    order_count int NOT NULL COMMENT '주문 수량',
    order_price int NOT NULL COMMENT '주문 가격',
    order_type enum('BUY','SELL') NOT NULL COMMENT '주문 타입',
    order_status enum('PENDING','COMPLETED','CANCELLED') NOT NULL COMMENT '주문 상태',
    order_executed_count int NOT NULL DEFAULT 0 COMMENT '체결된 수량',
    created_at timestamp NOT NULL COMMENT '주문 시간',
    updated_at timestamp NOT NULL COMMENT '수정 일자',
    stock_id bigint NOT NULL COMMENT '주식 아이디',
    user_id bigint NOT NULL COMMENT '회원 아이디',
    PRIMARY KEY (order_id),
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=INNODB COMMENT='주문';


-- 체결 테이블
CREATE TABLE IF NOT EXISTS execution (
    execution_id bigint NOT NULL AUTO_INCREMENT COMMENT '체결 아이디',
    execution_count int NOT NULL COMMENT '체결 수량',
    execution_price int NOT NULL COMMENT '체결 가격',
    created_at timestamp NOT NULL COMMENT '체결 시간',
    execution_buy_order_id bigint NOT NULL COMMENT '매수 주문 체결',
    execution_sell_order_id bigint NOT NULL COMMENT '매도 주문 체결',
    stock_id bigint NOT NULL COMMENT '주식 아이디',
    PRIMARY KEY (execution_id),
    FOREIGN KEY (execution_buy_order_id) REFERENCES order_table(order_id),
    FOREIGN KEY (execution_sell_order_id) REFERENCES order_table(order_id),
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
) ENGINE=INNODB COMMENT='체결';

-- 보유 주식 테이블
CREATE TABLE IF NOT EXISTS holding (
    holding_id bigint NOT NULL AUTO_INCREMENT COMMENT '보유주식 아이디',
    holding_total_price int NOT NULL COMMENT '총 손익',
    holding_quantity int NOT NULL COMMENT '보유 수량',
    updated_at timestamp NOT NULL COMMENT '수정 시간',
    user_id bigint NOT NULL COMMENT '회원 아이디',
    stock_id bigint NOT NULL COMMENT '주식 아이디',
    PRIMARY KEY (holding_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
) ENGINE=INNODB COMMENT='보유 주식';