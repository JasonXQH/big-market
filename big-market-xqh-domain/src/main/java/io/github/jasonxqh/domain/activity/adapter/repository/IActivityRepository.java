package io.github.jasonxqh.domain.activity.adapter.repository;


import io.github.jasonxqh.domain.activity.model.aggregate.CreateOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
public interface IActivityRepository {


    RaffleActivitySkuEntity queryActivitySku(Long sku);

    RaffleActivityEntity queryActivityByActivityId(Long activityId);

    RaffleActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);
}
