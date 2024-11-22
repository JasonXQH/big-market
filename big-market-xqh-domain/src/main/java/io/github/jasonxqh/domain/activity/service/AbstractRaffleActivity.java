package io.github.jasonxqh.domain.activity.service;

import com.alibaba.fastjson2.JSON;
import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractRaffleActivity implements IRaffleOrder {


    protected IActivityRepository activityRepository;

    public AbstractRaffleActivity(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }



    @Override
    public RaffleActivityOrderEntity createRaffleActivityOrder(RaffleActivityShopCartEntity shopCartEntity) {
        //1.通过sku查询活动信息
        RaffleActivitySkuEntity raffleActivitySkuEntity = activityRepository.queryActivitySku(shopCartEntity.getSku());
        log.info("查询结果：{} ", JSON.toJSON(raffleActivitySkuEntity));
        //2.查询活动信息
        RaffleActivityEntity activityEntity = activityRepository.queryActivityByActivityId(raffleActivitySkuEntity.getActivityId());
        //3.查询次数信息
        RaffleActivityCountEntity countEntity = activityRepository.queryRaffleActivityCountByActivityCountId(raffleActivitySkuEntity.getActivityCountId());
        log.info("查询结果：{} {} {} ", JSON.toJSON(raffleActivitySkuEntity), JSON.toJSON(activityEntity), JSON.toJSON(countEntity));


        return null;
    }
}
