package com.stockexchange.domain.order.dto;

import com.stockexchange.domain.order.entity.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderReqDTO {

    @NotNull(message = "주문 수량은 필수 입니다.")
    @Min(value = 1, message = "주문 수량은 1 이상 필수 입니다.")
    private int orderCount;
    @NotNull(message = "주문 가격은 필수 입니다.")
    @DecimalMin(value = "0.01", message = "주문 가격은 0보다 커야 합니다.")
    private BigDecimal orderPrice;
    @NotNull(message = "주문 타입은 필수 입니다.")
    private OrderType orderType;
    @NotNull(message = "종목 ID는 필수 입니다.")
    private Long stockId;
    @NotNull(message = "사용자 ID는 필수 입니다.")
    private Long userId;
}
