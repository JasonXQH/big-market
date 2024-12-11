package io.github.jasonxqh.domain.award.service;

import io.github.jasonxqh.domain.award.adapter.repository.IAwardRepository;
import io.github.jasonxqh.domain.award.event.SendAwardMessageEvent;
import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;
import io.github.jasonxqh.domain.award.model.entity.DeliverAwardEntity;
import io.github.jasonxqh.domain.award.model.entity.TaskEntity;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.vo.TaskStateVO;
import io.github.jasonxqh.domain.award.service.delivery.IDeliverAward;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.event.BaseEvent;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class AwardService implements IAwardService {

    private final IAwardRepository awardRepository;
    private final SendAwardMessageEvent sendAwardMessageEvent;
    private final Map<String,IDeliverAward> deliverAwardMap;
    public AwardService(IAwardRepository awardRepository, SendAwardMessageEvent sendAwardMessageEvent, Map<String, IDeliverAward> deliverAwardMap) {
        this.awardRepository = awardRepository;
        this.sendAwardMessageEvent = sendAwardMessageEvent;
        this.deliverAwardMap = deliverAwardMap;
    }


    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        //1.检验参数
        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();
        if(userId == null || activityId == null || awardId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        // 构建消息对象
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage = new SendAwardMessageEvent.SendAwardMessage();
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setAwardConfig(userAwardRecordEntity.getAwardConfig());
        sendAwardMessage.setOrderId(userAwardRecordEntity.getOrderId());
        sendAwardMessage.setAwardConfig(userAwardRecordEntity.getAwardConfig());
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);
        //3.构造Task
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userAwardRecordEntity.getUserId());
        taskEntity.setTopic(sendAwardMessageEvent.getTopic());
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        taskEntity.setMessage(sendAwardMessageEventMessage);
        taskEntity.setState(TaskStateVO.create);

        //构造Aggregate
        UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();
        //存储用户中奖单，但还没发放
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }

    @Override
    public void deliverAward(DeliverAwardEntity deliverAwardEntity) {
        Integer awardId = deliverAwardEntity.getAwardId();
        String awardKey = awardRepository.queryAwardKeyByAwardId(awardId);
        IDeliverAward iDeliverAward = deliverAwardMap.get(awardKey);
        if(iDeliverAward == null) {
            log.error("分发奖品，对应的服务不存在。awardKey:{}", awardKey);
            //TODO: 所有奖品都弄好了再打开
//            throw new RuntimeException("分发奖品，奖品" + awardKey + "对应的服务不存在");
            return;
        }
        //发放奖品
        iDeliverAward.giveOutPrizes(deliverAwardEntity);
    }
}
