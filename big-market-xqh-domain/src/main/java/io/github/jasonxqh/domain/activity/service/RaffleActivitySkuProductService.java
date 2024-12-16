package io.github.jasonxqh.domain.activity.service;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.entity.SkuProductEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RaffleActivitySkuProductService implements IRaffleActivitySkuProductService {
    @Resource
    private IActivityRepository activityRepository;
    @Override
    public List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId) {
        List<SkuProductEntity> skuProductEntityList = new ArrayList<>();
        List<RaffleActivitySkuEntity> raffleActivitySkuEntities = activityRepository.queryActivitySkuByActivityId(activityId);

        for(RaffleActivitySkuEntity raffleActivitySkuEntity : raffleActivitySkuEntities){
            Long activityCountId = raffleActivitySkuEntity.getActivityCountId();
            RaffleActivityCountEntity raffleActivityCountEntity = activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
            SkuProductEntity skuProductEntity = getSkuProductEntity(raffleActivitySkuEntity, raffleActivityCountEntity);
            skuProductEntityList.add(skuProductEntity);
        }

        return skuProductEntityList;
    }

    private static SkuProductEntity getSkuProductEntity(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        SkuProductEntity skuProductEntity = new SkuProductEntity();
        skuProductEntity.setSku(raffleActivitySkuEntity.getSku());
        skuProductEntity.setActivityId(raffleActivitySkuEntity.getActivityId());
        skuProductEntity.setActivityCountId(raffleActivitySkuEntity.getActivityCountId());
        skuProductEntity.setStockCount(raffleActivitySkuEntity.getStockCount());
        skuProductEntity.setStockCountSurplus(raffleActivitySkuEntity.getStockCountSurplus());
        skuProductEntity.setProductAmount(raffleActivitySkuEntity.getProductAmount());
        skuProductEntity.setActivityCount(raffleActivityCountEntity);
        return skuProductEntity;
    }
}
