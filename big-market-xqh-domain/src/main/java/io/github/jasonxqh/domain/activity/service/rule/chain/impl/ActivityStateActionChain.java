package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("activity_state_action")
public class ActivityStateActionChain  extends AbstractActionChain {
    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_BASE_ACTION.getCode();
    }

    @Override
    public boolean action(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityEntity raffleActivityEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        log.info("活动责任链-活动状态校验开始。");
        return true;
    }
}
