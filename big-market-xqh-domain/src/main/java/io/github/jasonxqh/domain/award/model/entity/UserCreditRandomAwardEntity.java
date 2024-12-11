package io.github.jasonxqh.domain.award.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreditRandomAwardEntity {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 奖品积分值
     */
    private BigDecimal creditAmount;
}
