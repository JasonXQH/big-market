package io.github.jasonxqh.domain.activity.adapter.repository;


import io.github.jasonxqh.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;

public interface IActivityRepository {


    RaffleActivitySkuEntity queryActivitySku(Long sku);

    RaffleActivityEntity queryActivityByActivityId(Long activityId);

    RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveSkuQuotaOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate);

    void storeActivitySkuStockCount(String key, Integer skuCount);


    void awardSkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void updateSkuStock(Long sku);

    Boolean substractionSkuStock(Long sku, String cacheKey, Date endDateTime);

    void clearQueueValue();

    void clearActivitySkuStock(Long sku);

    UserRaffleOrderEntity queryUnusedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    RaffleActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    RaffleActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    RaffleActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String month);

    void doSavePartakeOrder(CreatePartakeOrderAggregate partakeOrderAggregate);
}
