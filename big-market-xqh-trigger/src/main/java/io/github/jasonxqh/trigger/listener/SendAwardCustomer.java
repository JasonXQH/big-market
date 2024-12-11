package io.github.jasonxqh.trigger.listener;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.github.jasonxqh.domain.award.event.SendAwardMessageEvent;
import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;
import io.github.jasonxqh.domain.award.service.IAwardService;
import io.github.jasonxqh.domain.rebate.model.event.SendBehaviorRebateEvent;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SendAwardCustomer {
    @Value("${spring.rabbitmq.topic.send_award}")
    private String topic;
    @Resource
    private IAwardService awardService;

    @RabbitListener(queuesToDeclare = @Queue(value = "send_award"))
    public void listener(String message) {
        try {
            log.info("监听用户中奖记录消息 topic: {} message: {}", topic, message);
            BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage>>() {
            }.getType());
            SendAwardMessageEvent.SendAwardMessage sendAwardMessage = eventMessage.getData();
            Integer awardId = sendAwardMessage.getAwardId();
            String userId = sendAwardMessage.getUserId();
            String awardConfig = sendAwardMessage.getAwardConfig();
            String orderId = sendAwardMessage.getOrderId();
            DeliverAwardEntity deliverAwardEntity = DeliverAwardEntity.builder()
                    .awardId(awardId)
                    .userId(userId)
                    .awardConfig(awardConfig)
                    .orderId(orderId)
                    .build();
            //监听到了，就发奖
            awardService.deliverAward(deliverAwardEntity);
        } catch (Exception e) {
            log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, message);
            throw e;
        }
    }
}
