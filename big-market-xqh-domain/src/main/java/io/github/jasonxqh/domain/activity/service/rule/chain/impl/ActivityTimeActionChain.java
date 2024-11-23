package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component("activity_time_action")
public class   ActivityTimeActionChain extends AbstractActionChain {
    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_TIME_ACTION.getCode();
    }

    @Override
    public boolean action(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityEntity raffleActivityEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        log.info("活动责任链-活动时间校验开始。");
        return false;
    }
}
