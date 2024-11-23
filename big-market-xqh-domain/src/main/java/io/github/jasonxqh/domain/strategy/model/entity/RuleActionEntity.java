package io.github.jasonxqh.domain.strategy.model.entity;

import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description : 抽奖前、抽奖中、抽奖后，得到的不同结果
 * 传入参数必须是继承自RaffleEntity的
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionEntity<T extends  RuleActionEntity.RaffleEntity> {

    private String code = RuleLogicCheckTypeVO.ALLOW.getCode();
    private String info = RuleLogicCheckTypeVO.ALLOW.getInfo();
    private  String ruleModel;
    private T data;


    static public class RaffleEntity {

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static public class RaffleBeforeEntity extends RaffleEntity {
        //策略ID
        private Long strategyId;

        //权重值key：用于抽奖的时候可以过滤抽奖商品
        private String ruleWeightValueKey;

        //商品ID
        private Integer awardId;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static public class RaffleCenterEntity extends RaffleEntity {
        //策略ID
        private Long strategyId;

        //权重值key：用于抽奖的时候可以过滤抽奖商品
        private String ruleWeightValueKey;

        //商品ID
        private Integer awardId;
    }
    static public class RaffleAfterEntity extends RaffleEntity {

    }
}
