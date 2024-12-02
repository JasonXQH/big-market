package io.github.jasonxqh.domain.activity.service.armory;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ActivityArmoryDispatch implements IActivityDispatch,IActivitySkuArmory{
    @Resource
    IActivityRepository activityRepository;

    @Override
    public Boolean subtractionSkuStock(RaffleActivitySkuEntity raffleActivitySkuEntity, Date endDateTime) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + raffleActivitySkuEntity.getSku();
        return activityRepository.substractionSkuStock(raffleActivitySkuEntity,cacheKey,endDateTime);
    }

    @Override
    public boolean assembleActivitySku(Long sku) {
        //装配库存
        RaffleActivitySkuEntity raffleActivitySkuEntity = activityRepository.queryActivitySku(sku);
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        activityRepository.storeActivitySkuStockCount(cacheKey, raffleActivitySkuEntity.getStockCount());

        //预热活动，查询时预热到缓存
        activityRepository.queryActivityByActivityId(raffleActivitySkuEntity.getActivityId());
        activityRepository.queryRaffleActivityCountByActivityCountId(raffleActivitySkuEntity.getActivityCountId());

        return true;
    }

    @Override
    public boolean assembleActivitySkuByActivityId(Long activityId) {
        List<RaffleActivitySkuEntity>  raffleActivitySkuEntities = activityRepository.queryActivitySkuByActivityId(activityId);
        for(RaffleActivitySkuEntity raffleActivitySkuEntity:raffleActivitySkuEntities){
            cacheActivitySkuStockCount(raffleActivitySkuEntity.getSku(), raffleActivitySkuEntity.getStockCountSurplus());
            // 预热活动次数【查询时预热到缓存】
            activityRepository.queryRaffleActivityCountByActivityCountId(raffleActivitySkuEntity.getActivityCountId());
        }
        // 预热活动【查询时预热到缓存】
        activityRepository.queryActivityByActivityId(activityId);
        return true;
    }
    private void cacheActivitySkuStockCount(Long sku, Integer stockCount) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        activityRepository.cacheActivitySkuStockCount(cacheKey, stockCount);
    }

}
