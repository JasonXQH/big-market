package io.github.jasonxqh.domain.activity.adapter.repository;


import io.github.jasonxqh.domain.activity.model.aggregate.CreateOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuVO;

public interface IActivityRepository {


    RaffleActivitySkuEntity queryActivitySku(Long sku);

    RaffleActivityEntity queryActivityByActivityId(Long activityId);

    RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    void storeActivitySku(String key,Integer skuCount);

    Boolean substractionSkuStock(String cacheKey);

    void awardSkuStockConsumeSendQueue(ActivitySkuVO activitySkuVO);

    ActivitySkuVO takeQueueValue();

    void updateSkuStock(Long sku, Long activityId);
}
