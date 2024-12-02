package io.github.jasonxqh.trigger.http;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.api.IRaffleActivityService;
import io.github.jasonxqh.api.IRaffleStrategyService;
import io.github.jasonxqh.api.dto.*;
import io.github.jasonxqh.api.response.Response;
import io.github.jasonxqh.domain.activity.model.entity.PartakeRaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.UserRaffleOrderEntity;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityPartakeService;
import io.github.jasonxqh.domain.activity.service.armory.IActivitySkuArmory;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.vo.AwardStateVO;
import io.github.jasonxqh.domain.award.service.AwardService;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.service.IRaffleAward;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description :
 **/
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/raffle/activity")
public class RaffleActivityController implements IRaffleActivityService {
    private final IActivitySkuArmory activitySkuArmory;
    private final  IStrategyArmory strategyArmory;
    private final IRaffleStrategy strategyService;
    private final IRaffleActivityPartakeService activityPartakeService;
    private final AwardService awardService;

    public RaffleActivityController(IActivitySkuArmory activitySkuArmory, IStrategyArmory strategyArmory, IRaffleStrategy strategyService,  IRaffleActivityPartakeService activityPartakeService, AwardService awardService) {
        this.activitySkuArmory = activitySkuArmory;
        this.strategyArmory = strategyArmory;
        this.strategyService = strategyService;
        this.activityPartakeService = activityPartakeService;
        this.awardService = awardService;
    }


    @RequestMapping(value = "armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> activityArmory(@RequestParam Long activityId) {
        try{
            log.info("活动装配，数据预热，开始 activityId:{}", activityId);
            // 1. 活动装配
            activitySkuArmory.assembleActivitySkuByActivityId(activityId);
            // 2. 策略装配
            strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
            log.info("活动装配预热完成 activityId：{} response: {}", activityId, JSON.toJSONString(response));
            return response;
        }catch (Exception e){
            log.info("活动装配预热完成 activityId：{} response: {}", activityId,e);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
            return response;
        }
    }

    @RequestMapping(value = "draw", method = RequestMethod.POST)
    @Override
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO requestDTO) {
        try {
            //0.验证参数
            Long activityId = requestDTO.getActivityId();
            String userId = requestDTO.getUserId();
            if(activityId == null || userId == null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            //1. 构建抽奖单,此时已经执行抽奖次数扣减了
            UserRaffleOrderEntity orderEntity = activityPartakeService.createOrder(activityId, userId);
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}",userId,activityId, orderEntity.getOrderId());
            //2. 执行抽奖,消费抽奖单
            RaffleAwardEntity raffleAwardEntity = strategyService.performRaffle(RaffleFactorEntity.builder()
                    .strategyId(orderEntity.getStrategyId())
                    .userId(userId)
                    .endDateTime(orderEntity.getEndDateTime())
                    .build());

            //3.构造发奖记录，存储记录和Task，并更新抽奖单状态为已使用
            UserAwardRecordEntity userAwardRecord = UserAwardRecordEntity.builder()
                    .userId(orderEntity.getUserId())
                    .activityId(orderEntity.getActivityId())
                    .strategyId(orderEntity.getStrategyId())
                    .orderId(orderEntity.getOrderId())
                    .awardId(raffleAwardEntity.getAwardId())
                    .awardTitle(raffleAwardEntity.getAwardTitle())
                    .awardTime(new Date())
                    .awardState(AwardStateVO.create)
                    .build();
            awardService.saveUserAwardRecord(userAwardRecord);

            //4.返回结果
            Response<ActivityDrawResponseDTO> response = Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseDTO.builder()
                            .awardIndex(raffleAwardEntity.getSort())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardId(raffleAwardEntity.getAwardId())
                            .build())
                    .build();

            return response;
        }catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", requestDTO.getUserId(), requestDTO.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", requestDTO.getUserId(), requestDTO.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }


    }
}
