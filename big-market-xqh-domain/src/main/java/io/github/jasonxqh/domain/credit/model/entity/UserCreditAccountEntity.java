package io.github.jasonxqh.domain.credit.model.entity;

import io.github.jasonxqh.domain.award.model.vo.AccountStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreditAccountEntity {
    /**
     * 用户ID
     */
    private String userId;

    /**
     *   可用积分，每次需要调整的值
     */
    private BigDecimal adjustAmount;

}

