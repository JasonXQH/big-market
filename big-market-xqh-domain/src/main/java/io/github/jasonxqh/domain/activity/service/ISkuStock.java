package io.github.jasonxqh.domain.activity.service;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;

public interface ISkuStock {
    //获取奖品库存消耗队列
    ActivitySkuVO takeQueueValue() throws InterruptedException;
    //更新奖品库存消耗记录
    void updateStrategyAwardStock(Long sku,Long activityId);
}
