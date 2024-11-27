package io.github.jasonxqh.domain.activity.service.quota;

import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.domain.activity.model.valobj.OrderStateVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivitySkuStockService;
import io.github.jasonxqh.domain.activity.service.quota.rule.chain.factory.DefaultActionChainFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * @author jasonxu
 * @date 2024/11/23
 */
@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {

    public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActionChainFactory actionChainFactory) {
        super(activityRepository, actionChainFactory);
    }

    @Override
    protected CreateSkuQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, RaffleActivitySkuEntity activitySkuEntity, RaffleActivityEntity activityEntity, RaffleActivityCountEntity activityCountEntity) {

        RaffleActivityOrderEntity orderEntity = RaffleActivityOrderEntity.builder()
                .orderId(RandomStringUtils.randomAlphanumeric(12))
                .userId(skuRechargeEntity.getUserId())
                .sku(skuRechargeEntity.getSku())
                .activityId(activitySkuEntity.getActivityId())
                .activityName(activityEntity.getActivityName())
                .strategyId(activityEntity.getStrategyId())
                .orderTime(new Date())
                .totalCount(activityCountEntity.getTotalCount())
                .monthCount(activityCountEntity.getMonthCount())
                .dayCount(activityCountEntity.getDayCount())
                .state(OrderStateVO.completed)
                .outBusinessNo(skuRechargeEntity.getOutBusinessNo())
                .build();

        RaffleActivityAccountEntity accountActivity = RaffleActivityAccountEntity.builder()
                .userId(skuRechargeEntity.getUserId())
                .activityId(activityEntity.getActivityId())
                .totalCount(activityCountEntity.getTotalCount())
                .monthCount(activityCountEntity.getMonthCount())
                .dayCount(activityCountEntity.getDayCount())
                .build();

        return CreateSkuQuotaOrderAggregate.builder()
                .raffleActivityOrderEntity(orderEntity)
                .raffleActivityAccountEntity(accountActivity)
                .build();
    }

    @Override
    protected void doSaveOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate) {
        activityRepository.doSaveSkuQuotaOrder(createSkuQuotaOrderAggregate);
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
        return activityRepository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long sku) {
        activityRepository.updateSkuStock(sku);
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }
}
