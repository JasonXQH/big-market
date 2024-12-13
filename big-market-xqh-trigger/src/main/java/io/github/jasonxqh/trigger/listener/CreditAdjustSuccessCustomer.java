package io.github.jasonxqh.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.github.jasonxqh.domain.activity.model.entity.DeliveryOrderEntity;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.credit.event.CreditAdjustSuccessMessageEvent;
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
public class CreditAdjustSuccessCustomer {
    @Value("${spring.rabbitmq.topic.credit_adjust_success}")
    private String topic;

    @Resource
    private IRaffleActivityAccountQuotaService accountQuotaService;

    @RabbitListener(queuesToDeclare = @Queue(value = "credit_adjust_success"))
    public void listener(String message) {
        try {
            log.info("监听积分账户调整成功消息，进行交易商品发货(抽奖次数) topic: {} message: {}", topic, message);
            BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage>>() {
            }.getType());
            CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = eventMessage.getData();
            // 积分发货？
            DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity();
            deliveryOrderEntity.setUserId(creditAdjustSuccessMessage.getUserId());
            deliveryOrderEntity.setOutBusinessNo(creditAdjustSuccessMessage.getOutBusinessNo());
            accountQuotaService.updateOrder(deliveryOrderEntity);
        } catch (AppException e) {
            if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                log.warn("监听积分账户调整成功消息，进行交易商品发货(抽奖次数)，消费重复 topic: {} message: {}", topic, message, e);
                return;
            }
//            throw e;
        } catch (Exception e) {
            log.error("监听积分账户调整成功消息，进行交易商品发货失败 topic: {} message: {}", topic, message, e);
//            throw e;
        }
    }
}
