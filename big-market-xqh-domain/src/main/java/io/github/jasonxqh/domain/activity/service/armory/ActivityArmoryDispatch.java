package io.github.jasonxqh.domain.activity.service.armory;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ActivityArmoryDispatch implements IActivityDispatch,IActivitySkuArmory{
    @Resource
    IActivityRepository activityRepository;

    @Override
    public Boolean subtractionSkuStock(Long sku) {
        RaffleActivitySkuEntity raffleActivitySkuEntity = activityRepository.queryActivitySku(sku);
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + raffleActivitySkuEntity.getSku()+"_"+raffleActivitySkuEntity.getActivityId();
        return activityRepository.substractionSkuStock(cacheKey);
    }

    @Override
    public boolean assembleActivitySku(Long sku, Long activityId) {
        //装配库存
        RaffleActivitySkuEntity raffleActivitySkuEntity = activityRepository.queryActivitySku(sku);
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + raffleActivitySkuEntity.getSku()+"_"+raffleActivitySkuEntity.getActivityId();
        activityRepository.storeActivitySku(cacheKey, raffleActivitySkuEntity.getStockCountSurplus());
        return true;
    }
}
