package io.github.jasonxqh.domain.activity.service.rule.chain.impl;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityCountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivitySkuEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuVO;
import io.github.jasonxqh.domain.activity.service.armory.IActivityDispatch;
import io.github.jasonxqh.domain.activity.service.rule.chain.AbstractActionChain;
import io.github.jasonxqh.domain.activity.service.rule.chain.factory.DefaultActionChainFactory;
import io.github.jasonxqh.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
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
        log.info("活动责任链-商品库存处理【校验&扣减】开始。");
        //
        Boolean status = activityDispatch.subtractionSkuStock(raffleActivitySkuEntity.getSku());
        if(status){
            log.info("规则过滤-sku库存扣减成功，向redis延迟队列发送消息 ");
            activityRepository.awardSkuStockConsumeSendQueue(ActivitySkuVO.builder()
                    .sku(raffleActivitySkuEntity.getSku())
                    .activityId(raffleActivityCountEntity.getActivityCountId())
                    .build());
            return  true;
        }
        log.info("规则过滤-sku库存扣减失败");
        return false;
    }

    @Override
    protected String chainModel() {
        return DefaultActionChainFactory.ActionModel.ACTIVITY_SKU_STOCK_ACTION.getCode();
    }
}
