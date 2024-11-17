package io.github.jasonxqh.domain.strategy.service;

import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/14, 星期四
 * @Description :
 **/
public interface IRaffleStock {
    //获取奖品库存消耗队列
    StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException;

    //更新奖品库存消耗记录
    void updateStrategyAwardStock(Long strategyId,Integer awardId);
}
