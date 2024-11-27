package io.github.jasonxqh.domain.activity.service.quota;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.service.quota.rule.chain.factory.DefaultActionChainFactory;

public class RaffleActivityAccountQuotaSupport {
    protected IActivityRepository activityRepository;
    protected DefaultActionChainFactory actionChainFactory;

    public RaffleActivityAccountQuotaSupport(IActivityRepository activityRepository, DefaultActionChainFactory actionChainFactory) {
        this.actionChainFactory = actionChainFactory;
        this.activityRepository = activityRepository;
    }


    public RaffleActivitySkuEntity queryActivitySkuById(Long sku) {
        //1.通过sku查询活动信息
        return activityRepository.queryActivitySku(sku);
    }

    public RaffleActivityEntity queryActivityById(Long id) {
        return activityRepository.queryActivityByActivityId(id);
    }

    public RaffleActivityCountEntity queryActivityCount(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);

    }

}
