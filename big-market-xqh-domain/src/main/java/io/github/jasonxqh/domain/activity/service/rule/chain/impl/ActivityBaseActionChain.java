package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivityStateVO;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import io.github.jasonxqh.types.enums.ResponseCode;
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
        Date beginDateTime = raffleActivityEntity.getBeginDateTime();
        Date nowDateTime = new Date();
        // 检查当前时间是否早于结束时间
        if (nowDateTime.after(endDateTime)|| nowDateTime.before(beginDateTime)) {
            // 当前时间晚于活动结束时间，抛出异常或处理结束逻辑
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(),ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }
        log.info("活动在有效期内，继续责任链处理。");
        if(!ActivityStateVO.open.equals(raffleActivityEntity.getState())) {
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(),ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
        log.info("活动处于开启状态，继续责任链处理。");
        if(raffleActivitySkuEntity.getStockCountSurplus() <= 0)
            throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(),ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
        return next().action(raffleActivitySkuEntity, raffleActivityEntity, raffleActivityCountEntity);
    }

    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_BASE_ACTION.getCode();
    }
}
