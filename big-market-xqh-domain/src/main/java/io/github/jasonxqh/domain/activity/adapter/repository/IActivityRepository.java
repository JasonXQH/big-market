package io.github.jasonxqh.domain.activity.adapter.repository;


import cn.bugstack.middleware.db.router.annotation.DBRouter;
import io.github.jasonxqh.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

public interface IActivityRepository {


    RaffleActivitySkuEntity queryActivitySku(Long sku);

    RaffleActivityEntity queryActivityByActivityId(Long activityId);

    RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);


    void storeActivitySkuStockCount(String key, Integer skuCount);


    void awardSkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    List<ActivitySkuStockKeyVO> takeQueueValue();

    void updateSkuStock(Long sku, Long activityId);

    Boolean substractionSkuStock(RaffleActivitySkuEntity raffleActivitySku, String cacheKey, Date endDateTime);

    void clearQueueValue(ActivitySkuStockKeyVO skuStockKeyVO);

    void clearActivitySkuStock(ActivitySkuStockKeyVO skuStockKeyVO);

    UserRaffleOrderEntity queryUnusedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    RaffleActivityAccountEntity queryActivityAccountByUserIdAndActivityId(String userId, Long activityId);

    RaffleActivityAccountEntity queryActivityAccountByUserIdAndActivityIdFromFront(String userId, Long activityId);

    RaffleActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    RaffleActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String month);


    List<RaffleActivitySkuEntity> queryActivitySkuByActivityId(Long activityId);

    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    Integer queryActivityAccountPartakeCount(String userId, Long activityId);

    void doSaveNoPayOrder(CreateSkuQuotaOrderAggregate createQuotaOrderAggregate);
    void doSavePartakeOrder(CreatePartakeOrderAggregate partakeOrderAggregate);
    void doSaveCreditPayOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate);

    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);
}
