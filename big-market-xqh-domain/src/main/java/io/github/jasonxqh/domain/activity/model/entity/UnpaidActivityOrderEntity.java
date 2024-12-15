package io.github.jasonxqh.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnpaidActivityOrderEntity {
    private String userId;
    private String orderId;
    private BigDecimal payAmount;
    private String outBusinessNo;
}
