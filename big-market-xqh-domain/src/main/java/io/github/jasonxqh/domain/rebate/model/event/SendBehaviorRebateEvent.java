package io.github.jasonxqh.domain.rebate.model.event;

import io.github.jasonxqh.domain.rebate.model.vo.BehaviorTypeVO;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
public class SendBehaviorRebateEvent extends BaseEvent<SendBehaviorRebateEvent.SendBehaviorRebateMessage>  {
    @Value("${spring.rabbitmq.topic.send_behavior_rebate}")
    private String topic;

    @Override
    public BaseEvent.EventMessage<SendBehaviorRebateEvent.SendBehaviorRebateMessage> buildEventMessage(SendBehaviorRebateEvent.SendBehaviorRebateMessage data) {
        return BaseEvent.EventMessage.<SendBehaviorRebateEvent.SendBehaviorRebateMessage>builder()
                .id(RandomStringUtils.randomAlphanumeric(11))
                .data(data)
                .timestamp(new Date())
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SendBehaviorRebateMessage {
        /**
         * 用户ID
         */
        private String userId;
        /** 返利描述 */
        private String rebateDesc;
        /** 返利类型 */
        private String rebateType;
        /** 返利配置 */
        private String rebateConfig;
        /**
         * biz_id
         */
        private String biz_id;

    }
}
