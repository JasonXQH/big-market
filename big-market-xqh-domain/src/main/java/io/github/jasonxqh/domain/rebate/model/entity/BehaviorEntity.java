package io.github.jasonxqh.domain.rebate.model.entity;

import io.github.jasonxqh.domain.rebate.model.vo.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorEntity {
    /**
    *  用户Id
    * */
    private String userId;
    /**
     * 行为类型（sign 签到、openai_pay 支付）
     */
    private BehaviorTypeVO behaviorType;
    /**
     * 业务ID；签到则是日期字符串，支付则是外部的业务ID
     */
    private String outBusinessNo;
}
