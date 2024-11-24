package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivityStateVO;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    @Override
    public boolean action(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityEntity raffleActivityEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        log.info("活动责任链-基础信息【有效期、状态】校验开始。");
        Date endDateTime = raffleActivityEntity.getEndDateTime();
        // 检查当前时间是否早于结束时间
        if ( new Date().before(endDateTime)) {
            log.info("活动在有效期内，继续责任链处理。");
        } else {
            // 当前时间晚于活动结束时间，抛出异常或处理结束逻辑
            throw new AppException("活动已过期，无法进行操作");
        }
        if(raffleActivityEntity.getState().getCode().equals(ActivityStateVO.create.getCode())){
            log.info("活动状态正确，继续责任链处理。");
        }else{
            return false;
        }
        return next().action(raffleActivitySkuEntity, raffleActivityEntity, raffleActivityCountEntity);
    }

    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_BASE_ACTION.getCode();
    }
}
