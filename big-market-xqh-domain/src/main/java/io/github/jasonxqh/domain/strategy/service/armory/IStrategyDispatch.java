package io.github.jasonxqh.domain.strategy.service.armory;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/8, 星期五
 * @Description : 策略抽奖的调度
 **/
public interface IStrategyDispatch {
    /*
    * 获取抽奖策略装配的随机结果
    *
    * */
    Integer getRandomAwardId(Long strategyId);

    Integer getRandomAwardId(Long strategyId, String weightValue);
}
