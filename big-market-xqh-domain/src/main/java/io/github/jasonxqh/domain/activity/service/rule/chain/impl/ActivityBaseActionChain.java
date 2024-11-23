package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    @Override
    public boolean action(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityEntity raffleActivityEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        log.info("活动责任链-基础信息【有效期、状态】校验开始。");

        return next().action(raffleActivitySkuEntity, raffleActivityEntity, raffleActivityCountEntity);
    }

    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_BASE_ACTION.getCode();
    }
}
