package io.github.jasonxqh.trigger.listener;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.github.jasonxqh.domain.activity.model.entity.SkuRechargeEntity;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.rebate.model.event.SendBehaviorRebateEvent;
import io.github.jasonxqh.domain.rebate.model.vo.RebateTypeVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.event.BaseEvent;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SendBehaviorRebateCustomer {
    @Value("${spring.rabbitmq.topic.send_behavior_rebate}")
    private String topic;

    @Resource
    private IRaffleActivityAccountQuotaService accountQuotaService;

    //监听到了，就执行用户返利
    @RabbitListener(queuesToDeclare = @Queue(value = "send_behavior_rebate"))
    public void listener(String message) {
        try {
            BaseEvent.EventMessage<SendBehaviorRebateEvent.SendBehaviorRebateMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendBehaviorRebateEvent.SendBehaviorRebateMessage>>() {
            }.getType());
            SendBehaviorRebateEvent.SendBehaviorRebateMessage behaviorRebateMessage = eventMessage.getData();
            String userId = behaviorRebateMessage.getUserId();
            //如9011，就是sku的编号9011
            String rebateConfig = behaviorRebateMessage.getRebateConfig();
            String bizId = behaviorRebateMessage.getBiz_id();
            if (RebateTypeVO.SKU.getCode().equals(behaviorRebateMessage.getRebateType())) {
                SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
                skuRechargeEntity.setSku(Long.valueOf(rebateConfig));
                skuRechargeEntity.setUserId(userId);
                skuRechargeEntity.setOutBusinessNo(bizId);
                String order = accountQuotaService.createOrder(skuRechargeEntity);
                log.info("监听用户行为返利记录消息，增加用户配额 topic: {} userId: {} bizId{} order:{}", topic, userId, bizId,order);
            }else if(RebateTypeVO.INTEGRAL.getCode().equals(behaviorRebateMessage.getRebateType())) {
                //用户积分奖励

            }

        } catch (AppException e) {
            if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                log.warn("监听用户行为返利消息，消费重复 topic: {} message: {}", topic, message, e);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
            throw e;
        }
    }
}
