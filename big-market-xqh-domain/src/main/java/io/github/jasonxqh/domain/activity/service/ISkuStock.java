package io.github.jasonxqh.domain.activity.service;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;

public interface ISkuStock {
    //获取奖品库存消耗队列
    ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException;
    //更新奖品库存消耗记录
    void updateStrategyAwardStock(Long sku);

    /**
     * 清空队列
     */
    void clearQueueValue();
    /**
     * 缓存库存以消耗完毕，清空数据库库存
     *
     * @param sku 活动商品
     */
    void clearActivitySkuStock(Long sku);

}
