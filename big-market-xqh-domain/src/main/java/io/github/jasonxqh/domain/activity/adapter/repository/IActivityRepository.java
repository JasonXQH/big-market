package io.github.jasonxqh.domain.activity.adapter.repository;


import io.github.jasonxqh.domain.activity.model.aggregate.CreateOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;

public interface IActivityRepository {


    RaffleActivitySkuEntity queryActivitySku(Long sku);

    RaffleActivityEntity queryActivityByActivityId(Long activityId);

    RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    void storeActivitySkuStockCount(String key, Integer skuCount);


    void awardSkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void updateSkuStock(Long sku);

    Boolean substractionSkuStock(Long sku, String cacheKey, Date endDateTime);

    void clearQueueValue();

    void clearActivitySkuStock(Long sku);
}
