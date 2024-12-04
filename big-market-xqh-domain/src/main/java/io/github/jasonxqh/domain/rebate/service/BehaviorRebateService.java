package io.github.jasonxqh.domain.rebate.service;

import io.github.jasonxqh.domain.rebate.adapter.repository.IBehaviorRebateRepository;
import io.github.jasonxqh.domain.rebate.model.aggregate.BehaviorRebateOrderAggregate;
import io.github.jasonxqh.domain.rebate.model.entity.BehaviorEntity;
import io.github.jasonxqh.domain.rebate.model.entity.TaskEntity;
import io.github.jasonxqh.domain.rebate.model.entity.UserBehaviorRebateOrderEntity;
import io.github.jasonxqh.domain.rebate.model.event.SendBehaviorRebateEvent;
import io.github.jasonxqh.domain.rebate.model.vo.BehaviorTypeVO;
import io.github.jasonxqh.domain.rebate.model.vo.DailyBehaviorRebateVO;
import io.github.jasonxqh.domain.rebate.model.vo.TaskStateVO;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.event.BaseEvent;
import io.github.jasonxqh.types.exception.AppException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public  class BehaviorRebateService implements IBehaviorRebateService {

    @Resource
    private IBehaviorRebateRepository rebateRepository;

    @Resource
    private SendBehaviorRebateEvent sendBehaviorRebateEvent;

    @Override
    public List<String> createOrder(BehaviorEntity behaviorEntity) {
        //0.数据校验
        String userId = behaviorEntity.getUserId();
        BehaviorTypeVO behaviorType = behaviorEntity.getBehaviorType();
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = rebateRepository.queryDailyBehaviorRebateByBehaviorType(behaviorType.getCode());


        if(StringUtils.isBlank(userId)||behaviorType==null){
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        if (null == dailyBehaviorRebateVOS || dailyBehaviorRebateVOS.isEmpty()) return new ArrayList<>();

        List<String> orderIds = new ArrayList<>();
        List<BehaviorRebateOrderAggregate> behaviorRebateOrderAggregates = new ArrayList<>();

        //1.根据behaviorType获取DailyBehaviorRebate

        //2.为每个vo 构建 聚合对象
        for(DailyBehaviorRebateVO dailyBehaviorRebateVO : dailyBehaviorRebateVOS){
            UserBehaviorRebateOrderEntity userBehaviorRebateOrderEntity = new UserBehaviorRebateOrderEntity();
            //2.1 构建当日唯一行为ID

            String biz_id = userId+ Constants.UNDERLINE + dailyBehaviorRebateVO.getRebateType()+Constants.UNDERLINE+behaviorEntity.getOutBusinessNo();
            //构建 rebate 订单
            userBehaviorRebateOrderEntity.setUserId(userId);
            userBehaviorRebateOrderEntity.setBehaviorType(behaviorType.getCode());
            userBehaviorRebateOrderEntity.setBizId(biz_id);
            userBehaviorRebateOrderEntity.setOutBusinessNo(behaviorEntity.getOutBusinessNo());
            userBehaviorRebateOrderEntity.setOrderId(RandomStringUtils.randomAlphanumeric(12));
            userBehaviorRebateOrderEntity.setRebateConfig(dailyBehaviorRebateVO.getRebateConfig());
            userBehaviorRebateOrderEntity.setRebateDesc(dailyBehaviorRebateVO.getRebateDesc());
            userBehaviorRebateOrderEntity.setRebateType(dailyBehaviorRebateVO.getRebateType());

            //2.构造消息对象
            SendBehaviorRebateEvent.SendBehaviorRebateMessage sendBehaviorRebateMessage = new  SendBehaviorRebateEvent.SendBehaviorRebateMessage();
            sendBehaviorRebateMessage.setUserId(userId);
            sendBehaviorRebateMessage.setRebateDesc(dailyBehaviorRebateVO.getRebateDesc());
            sendBehaviorRebateMessage.setRebateConfig(dailyBehaviorRebateVO.getRebateConfig());
            sendBehaviorRebateMessage.setRebateType(dailyBehaviorRebateVO.getRebateType());
            sendBehaviorRebateMessage.setBiz_id(biz_id);

            BaseEvent.EventMessage<SendBehaviorRebateEvent.SendBehaviorRebateMessage> sendBehaviorRebateMessageEventMessage = sendBehaviorRebateEvent.buildEventMessage(sendBehaviorRebateMessage);
            //3.构造Task
            TaskEntity task = TaskEntity.builder()
                    .userId(userId)
                    .topic(sendBehaviorRebateEvent.getTopic())
                    .messageId(sendBehaviorRebateMessageEventMessage.getId())
                    .message(sendBehaviorRebateMessageEventMessage)
                    .state(TaskStateVO.create)
                    .build();

            //3.构造Aggregate
            BehaviorRebateOrderAggregate behaviorRebateOrderAggregate = BehaviorRebateOrderAggregate.builder()
                    .userBehaviorRebateOrder(userBehaviorRebateOrderEntity)
                    .taskEntity(task)
                    .build();

            behaviorRebateOrderAggregates.add(behaviorRebateOrderAggregate);
            orderIds.add(userBehaviorRebateOrderEntity.getOrderId());
        }

        //4.存入Aggregate
        rebateRepository.doSaveRebateOrderAggregate(userId,behaviorRebateOrderAggregates);
        return orderIds;
    }

    @Override
    public Integer queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        return rebateRepository.queryOrderByOutBusinessNo(userId,outBusinessNo);
    }
}
