package com.stockexchange.common.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    //    성공 응답
    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.OK, "리소스가 성공적으로 생성되었습니다."),

    //    주문 관련
    ORDER_CREATED(HttpStatus.OK, "주문이 생성되었습니다."),
    ORDER_UPDATED(HttpStatus.OK, "주문이 수정되었습니다."),
    ORDER_CANCELED(HttpStatus.OK, "주문이 취소되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
