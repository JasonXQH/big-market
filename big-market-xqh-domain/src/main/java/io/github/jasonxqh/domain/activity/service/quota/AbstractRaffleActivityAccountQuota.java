package io.github.jasonxqh.domain.activity.service.quota;

import com.alibaba.fastjson2.JSON;
import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.aggregate.CreateSkuQuotaOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.activity.service.quota.policy.ITradePolicy;
import io.github.jasonxqh.domain.activity.service.quota.rule.chain.IActionChain;
import io.github.jasonxqh.domain.activity.service.quota.rule.chain.factory.DefaultActionChainFactory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

    private final Map<String, ITradePolicy> tradePolicyGroup;

    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActionChainFactory actionChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
        super(activityRepository, actionChainFactory);
        this.tradePolicyGroup = tradePolicyGroup;
    }

    @Override
    public RaffleActivityOrderEntity createRaffleActivityOrder(RaffleActivityShopCartEntity shopCartEntity) {
        //1.通过sku查询活动信息
        RaffleActivitySkuEntity raffleActivitySkuEntity = activityRepository.queryActivitySku(shopCartEntity.getSku());
        //2.查询活动信息
        RaffleActivityEntity activityEntity = activityRepository.queryActivityByActivityId(raffleActivitySkuEntity.getActivityId());
        //3.查询次数信息
        RaffleActivityCountEntity countEntity = activityRepository.queryRaffleActivityCountByActivityCountId(raffleActivitySkuEntity.getActivityCountId());
        log.info("查询结果：{} {} {} ", JSON.toJSON(raffleActivitySkuEntity), JSON.toJSON(activityEntity), JSON.toJSON(countEntity));
        return null;
    }
    //用户要签到、打卡、下单 等行为，才会创建一个订单，实现SKU库存扣减和增加抽奖次数
    @Override
    public UnpaidActivityOrderEntity createOrder(SkuRechargeEntity skuRechargeEntity) {

        //1.参数校验
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
        if(null == sku || StringUtils.isEmpty(userId) ||StringUtils.isEmpty(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //2.查询基础信息
        //2.1根据sku查询sku信息
        RaffleActivitySkuEntity raffleActivitySkuEntity = queryActivitySkuById(sku);
        //2.2查询活动信息
        RaffleActivityEntity activityEntity = queryActivityById(raffleActivitySkuEntity.getActivityId());
        //2.3查询次数信息，即用户在活动上可参与的次数
        RaffleActivityCountEntity countEntity = queryActivityCount(raffleActivitySkuEntity.getActivityCountId());
        //3.责任链校验
        IActionChain actionChain = actionChainFactory.openLogicChain();
        //检验成功，则会扣减activity的sku库存，防止超卖(超卖次数)
        boolean success = actionChain.action(raffleActivitySkuEntity, activityEntity, countEntity);
        //4. 构建抽奖单聚合对象，增加用户的抽奖次数。
        CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate = buildOrderAggregate(skuRechargeEntity, raffleActivitySkuEntity, activityEntity, countEntity);
        // 5. 交易策略 - 【积分兑换，支付类订单】【返利无支付交易订单，直接充值到账】【订单状态变更交易类型策略】
        ITradePolicy tradePolicy = tradePolicyGroup.get(skuRechargeEntity.getOrderTradeType().getCode());
        tradePolicy.trade(createSkuQuotaOrderAggregate);
        //6.返回单号
        return UnpaidActivityOrderEntity.builder()
                .userId(userId)
                .orderId(createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getOrderId())
                .outBusinessNo(outBusinessNo)
                .payAmount(createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getPayAmount())
                .build();
//        return createSkuQuotaOrderAggregate.getRaffleActivityOrderEntity().getOrderId();
    }


    protected abstract CreateSkuQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, RaffleActivitySkuEntity activitySkuEntity, RaffleActivityEntity  activityEntity, RaffleActivityCountEntity activityCountEntity);

//    protected abstract void doSaveOrder(CreateSkuQuotaOrderAggregate createSkuQuotaOrderAggregate);
}
