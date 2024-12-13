package io.github.jasonxqh.domain.credit.model.aggregate;

import io.github.jasonxqh.domain.credit.event.CreditAdjustSuccessMessageEvent;
import io.github.jasonxqh.domain.credit.model.entity.TaskEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditOrderEntity;
import io.github.jasonxqh.domain.credit.model.vo.TaskStateVO;
import io.github.jasonxqh.domain.credit.model.vo.TradeNameVO;
import io.github.jasonxqh.domain.credit.model.vo.TradeTypeVO;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeAggregate {
    private String userId;
    private UserCreditAccountEntity userCreditAccount;
    private UserCreditOrderEntity userCreditOrder;
    private TaskEntity task;
    public static UserCreditOrderEntity buildUserCreditOrderEntity(String userId,
                                                                   TradeNameVO tradeName,
                                                                   TradeTypeVO tradeType,
                                                                   BigDecimal tradeAmount,
                                                                   String outBusinessNo){
        return  UserCreditOrderEntity.builder()
                .userId(userId)
                .tradeName(tradeName)
                .tradeType(tradeType)
                .tradeAmount(tradeAmount)
                .outBusinessNo(outBusinessNo)
                .orderId(RandomStringUtils.randomAlphanumeric(12))
                .build();
    }

    public static UserCreditAccountEntity buildUserCreditAccountEntity(String userId,BigDecimal amount){
        return UserCreditAccountEntity.builder()
                .userId(userId)
                .adjustAmount(amount)
                .build();
    }

    public static TaskEntity buildTaskEntity(String userId,String messageId, String topic, BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> skuCreditConsumeMessage){
        return TaskEntity.builder()
                .userId(userId)
                .topic(topic)
                .messageId(messageId)
                .message(skuCreditConsumeMessage)
                .state(TaskStateVO.create)
                .build();
    }

}
