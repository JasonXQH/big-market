package io.github.jasonxqh.domain.award.model.entity;

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
     * 总积分，显示总账户值，记得一个人获得的总积分
     */
    private BigDecimal totalAmount;

    /**
     * 可用积分，每次扣减的值
     */
    private BigDecimal availableAmount;

    /**
     * 账户状态【open - 可用，close - 冻结】
     */
    private AccountStatusVO accountStatus;

}

