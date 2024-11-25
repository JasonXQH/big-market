package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.domain.activity.service.armory.IActivityDispatch;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {
    @Resource
    private IActivityRepository activityRepository;

    @Resource
    private IActivityDispatch activityDispatch;

    @Override
    public boolean action(RaffleActivitySkuEntity raffleActivitySkuEntity, RaffleActivityEntity raffleActivityEntity, RaffleActivityCountEntity raffleActivityCountEntity) {
        log.info("活动责任链-商品库存处理【校验&扣减】开始。sku:{},activityId:{}",raffleActivitySkuEntity.getSku(),raffleActivitySkuEntity.getActivityId());
        //sku库存扣减
        Boolean status = activityDispatch.subtractionSkuStock(raffleActivitySkuEntity.getSku(),raffleActivityEntity.getEndDateTime());
        if(status){
            log.info("规则过滤-sku库存扣减成功，向redis延迟队列发送消息 ");
            activityRepository.awardSkuStockConsumeSendQueue(ActivitySkuStockKeyVO.builder()
                    .sku(raffleActivitySkuEntity.getSku())
                    .activityId(raffleActivityCountEntity.getActivityCountId())
                    .build());
            return  true;
        }
        log.info("规则过滤-sku库存扣减失败");
        throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(),ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
    }

    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_SKU_STOCK_ACTION.getCode();
    }
}
