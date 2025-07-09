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
    user_code bigint NOT NULL AUTO_INCREMENT COMMENT '회원 코드',
    user_name varchar(20) NOT NULL COMMENT '회원명',
    user_price bigint NOT NULL COMMENT '보유 현금',
    created_at datetime NOT NULL COMMENT '가입 시간',
    PRIMARY KEY (user_code)
) ENGINE=INNODB COMMENT='회원';

-- 주식 종목 테이블
CREATE TABLE IF NOT EXISTS stock (
    stock_code bigint NOT NULL AUTO_INCREMENT COMMENT '주식 코드',
    stock_name varchar(255) NOT NULL COMMENT '종목명',
    stock_number int NOT NULL COMMENT '종목 코드',
    stock_ipo enum('KOSPI','KOSDAQ') NOT NULL COMMENT '상장시장',
    stock_price int NOT NULL COMMENT '종목 가격',
    stock_before_price int NOT NULL COMMENT '전일가격',
    stock_start_price int NOT NULL COMMENT '시가',
    stock_high_price int NOT NULL COMMENT '고가',
    stock_low_price int NOT NULL COMMENT '저가',
    stock_volume int NOT NULL COMMENT '거래량',
    PRIMARY KEY (stock_code)
) ENGINE=INNODB COMMENT='주식 종목';

-- 주문 테이블 
CREATE TABLE IF NOT EXISTS order_table (
    order_code bigint NOT NULL AUTO_INCREMENT COMMENT '주문 코드',
    order_count int NOT NULL COMMENT '주문 수량',
    order_price int NOT NULL COMMENT '주문 가격',
    order_type enum('BUY','SELL') NOT NULL COMMENT '주문 타입',
    order_status enum('PENDING','PARTIAL','COMPLETED','CANCELLED') NOT NULL COMMENT '주문상태',
    created_at datetime NOT NULL COMMENT '주문 시간',
    updated_at datetime NOT NULL COMMENT '수정 일자',
    stock_code bigint NOT NULL COMMENT '주식 코드',
    user_code bigint NOT NULL COMMENT '회원 코드',
    PRIMARY KEY (order_code),
    FOREIGN KEY (stock_code) REFERENCES stock(stock_code),
    FOREIGN KEY (user_code) REFERENCES user(user_code)
) ENGINE=INNODB COMMENT='주문';


-- 체결 테이블
CREATE TABLE IF NOT EXISTS execution (
    execution_code bigint NOT NULL AUTO_INCREMENT COMMENT '체결 코드',
    execution_count int NOT NULL COMMENT '체결 수량',
    execution_price int NOT NULL COMMENT '체결 가격',
    created_at datetime NOT NULL COMMENT '체결 시간',
    execution_buy bigint NOT NULL COMMENT '매수 주문 체결',
    execution_sell bigint NOT NULL COMMENT '매도 주문 체결',
    stock_code bigint NOT NULL COMMENT '주식 코드',
    PRIMARY KEY (execution_code),
    FOREIGN KEY (execution_buy) REFERENCES order_table(order_code),
    FOREIGN KEY (execution_sell) REFERENCES order_table(order_code),
    FOREIGN KEY (stock_code) REFERENCES stock(stock_code)
) ENGINE=INNODB COMMENT='체결';

-- 보유주식 테이블
CREATE TABLE IF NOT EXISTS holding (
    holding_code bigint NOT NULL AUTO_INCREMENT COMMENT '보유주식 코드',
    holding_total_price int NOT NULL COMMENT '총 손익',
    holding_quantity int NOT NULL COMMENT '보유 수량',
    updated_at datetime NOT NULL COMMENT '수정 시간',
    user_code bigint NOT NULL COMMENT '회원 코드',
    stock_code bigint NOT NULL COMMENT '주식 코드',
    PRIMARY KEY (holding_code),
    FOREIGN KEY (user_code) REFERENCES user(user_code),
    FOREIGN KEY (stock_code) REFERENCES stock(stock_code)
) ENGINE=INNODB COMMENT='보유 주식';