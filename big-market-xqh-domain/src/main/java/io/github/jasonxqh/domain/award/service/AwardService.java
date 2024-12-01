package io.github.jasonxqh.domain.award.service;

import io.github.jasonxqh.domain.award.adapter.repository.IAwardRepository;
import io.github.jasonxqh.domain.award.event.SendAwardMessageEvent;
import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;
import io.github.jasonxqh.domain.award.model.entity.TaskEntity;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.vo.TaskStateVO;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.event.BaseEvent;
import io.github.jasonxqh.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public  class AwardService implements IAwardService {

    @Resource
    private IAwardRepository awardRepository;
    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;

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
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());

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
        //事务存入
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }
}
