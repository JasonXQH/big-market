package io.github.jasonxqh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateRequestDTO {
    /**
     *  用户Id
     * */
    private String userId;
    /**
     * 行为类型（sign 签到、openai_pay 支付）
     */
    private String behaviorType;
}
