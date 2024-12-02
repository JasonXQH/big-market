package io.github.jasonxqh.domain.activity.service;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.List;

public interface IRaffleActivitySkuStockService {
    //获取奖品库存消耗队列
    List<ActivitySkuStockKeyVO> takeQueueValues() throws InterruptedException;
    //更新奖品库存消耗记录
    void updateStrategyAwardStock(Long sku, Long activityId);

    /**
     * 清空队列
     */
    void clearQueueValue(ActivitySkuStockKeyVO skuStockKeyVO);
    /**
     * 缓存库存以消耗完毕，清空数据库库存
     *
     * @param skuStockKeyVO
     */
    void clearActivitySkuStock(ActivitySkuStockKeyVO skuStockKeyVO);

}
