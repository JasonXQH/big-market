package io.github.jasonxqh.domain.credit.event;

import io.github.jasonxqh.types.event.BaseEvent;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author jasonxu
 */
@Component
public class CreditAdjustSuccessMessageEvent extends BaseEvent<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage>{

    @Value("${spring.rabbitmq.topic.credit_adjust_success}")
    private String topic;


    @Override
    public EventMessage<CreditAdjustSuccessMessage> buildEventMessage(CreditAdjustSuccessMessage data) {
        return EventMessage.<CreditAdjustSuccessMessage>builder()
                .id(RandomStringUtils.randomAlphanumeric(10))
                .timestamp(new Date())
                .data(data)
                .build();
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreditAdjustSuccessMessage {
        /**
         * 用户ID
         */
        private String userId;
        /**
         * 订单ID
         */
        private String orderId;
        /**
         * 交易积分
         */
        private BigDecimal amount;
        /**
         * 业务仿重ID - 外部透传。返利、行为等唯一标识
         */
        private String outBusinessNo;
    }

    @Override
        public String topic() {
            return topic;
        }

}
