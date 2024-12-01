package io.github.jasonxqh.domain.strategy.service.armory;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 装配
 **/
public interface IStrategyArmory {

    boolean assembleLotteryStrategy(Long strategyId);

    boolean assembleLotteryStrategyByActivityId(Long activityId);
}
