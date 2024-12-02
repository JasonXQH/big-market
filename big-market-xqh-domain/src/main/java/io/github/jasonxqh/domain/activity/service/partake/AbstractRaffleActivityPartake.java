package io.github.jasonxqh.domain.activity.service.partake;

import com.alibaba.fastjson2.JSON;
import io.github.jasonxqh.domain.activity.adapter.repository.IActivityRepository;
import io.github.jasonxqh.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import io.github.jasonxqh.domain.activity.model.entity.PartakeRaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.UserRaffleOrderEntity;
import io.github.jasonxqh.domain.activity.model.valobj.ActivityStateVO;
import io.github.jasonxqh.domain.activity.model.valobj.UserRaffleOrderStateVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityPartakeService;
import io.github.jasonxqh.types.common.OrderIdGenerator;
import io.github.jasonxqh.types.common.SnowFlake;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author jasonxu
 * @date 2024/11/26
 */

@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {
    SnowFlake worker = new SnowFlake(1, 1);
    protected final IActivityRepository activityRepository;

    protected AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * 创建订单的流程
     * @param partakeRaffleActivityEntity
     * @return {@link UserRaffleOrderEntity }
     */
    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        //0.基础信息判断
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        String userId = partakeRaffleActivityEntity.getUserId();
        Date currentDate = new Date();
        if (activityId != null && userId != null) {
            StringUtils.isEmpty(userId);
        }

        //1.活动查询基础
        RaffleActivityEntity raffleActivityEntity = activityRepository.queryActivityByActivityId(activityId);
        //校验活动状态
        if(currentDate.before(raffleActivityEntity.getBeginDateTime())||currentDate.after(raffleActivityEntity.getEndDateTime())){
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(),ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }
        //校验活动日期
        if(!raffleActivityEntity.getState().equals(ActivityStateVO.open)){
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(),ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
        //2.查询未被使用的活动参与订单记录
        UserRaffleOrderEntity userRaffleOrderEntity =  activityRepository.queryUnusedRaffleOrder(partakeRaffleActivityEntity);

        if(userRaffleOrderEntity != null){
            log.info("创建参与活动订单[已存在，未消费] userId:{} activityId:{} userRaffleOrderEntity:{}",userId,activityId, JSON.toJSON(userRaffleOrderEntity));
            return userRaffleOrderEntity;

        }
        //3.账户额度过滤
        CreatePartakeOrderAggregate partakeOrderAggregate =  this.doFilterAccount(userId,activityId,currentDate);

        //4.构造订单实体 OrderEntity
        userRaffleOrderEntity = UserRaffleOrderEntity.builder()
                .activityId(activityId)
                .userId(userId)
                .strategyId(raffleActivityEntity.getStrategyId())
                .activityName(raffleActivityEntity.getActivityName())
                .orderId(worker.nextId())
                .orderState(UserRaffleOrderStateVO.create)
                .orderTime(currentDate)
                .endDateTime(raffleActivityEntity.getEndDateTime())
                .build();

        //5.填充抽奖单实体对象
        partakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrderEntity);

        //5.订单落库
        activityRepository.doSavePartakeOrder(partakeOrderAggregate);

        //创建订单
        return userRaffleOrderEntity;
    }

    @Override
    public UserRaffleOrderEntity createOrder(Long activityId, String userId) {
        return this.createOrder(PartakeRaffleActivityEntity.builder()
                .activityId(activityId)
                .userId(userId)
                .build());
    }

    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);


}
