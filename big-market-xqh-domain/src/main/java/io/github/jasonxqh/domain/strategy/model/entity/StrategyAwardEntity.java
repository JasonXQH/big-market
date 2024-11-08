package io.github.jasonxqh.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description :
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity {
    /*抽奖策略ID*/
    private Long strategyId;
    /*抽奖奖品ID【规则类型为策略，则不需要奖品ID】*/
    private Integer awardId;
    /*奖品库存总量*/
    private Integer awardCount;
    /*奖品库存剩余*/
    private Integer awardCountSurplus;
    /*奖品中奖概率*/
    private BigDecimal awardRate;
}
