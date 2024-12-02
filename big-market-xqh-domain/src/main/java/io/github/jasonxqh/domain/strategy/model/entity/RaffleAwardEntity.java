package io.github.jasonxqh.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description : 抽奖奖品返回实体,但不是奖品发放逻辑
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity {
    private Long strategyId;
    private int awardId;
    /** 奖品配置*/
    private String awardConfig;
    /** 抽奖奖品标题 */
    private String awardTitle;
    /** 奖品顺序号 */
    private Integer sort;
    /** 活动结束时间*/
    private Date endDateTime;
}
