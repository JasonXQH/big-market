package io.github.jasonxqh.trigger.http;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.api.IRaffleActivityService;
import io.github.jasonxqh.api.dto.*;
import io.github.jasonxqh.api.response.Response;
import io.github.jasonxqh.domain.activity.model.entity.*;
import io.github.jasonxqh.domain.activity.model.valobj.OrderTradeTypeVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityPartakeService;
import io.github.jasonxqh.domain.activity.service.IRaffleActivitySkuProductService;
import io.github.jasonxqh.domain.activity.service.armory.IActivitySkuArmory;
import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.vo.AwardStateVO;
import io.github.jasonxqh.domain.award.service.AwardService;
import io.github.jasonxqh.domain.credit.model.entity.TradeEntity;
import io.github.jasonxqh.domain.credit.model.entity.UserCreditAccountEntity;
import io.github.jasonxqh.domain.credit.model.vo.TradeNameVO;
import io.github.jasonxqh.domain.credit.model.vo.TradeTypeVO;
import io.github.jasonxqh.domain.credit.service.ICreditAdjustService;
import io.github.jasonxqh.domain.rebate.model.entity.BehaviorEntity;
import io.github.jasonxqh.domain.rebate.model.vo.BehaviorTypeVO;
import io.github.jasonxqh.domain.rebate.service.IBehaviorRebateService;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
    private final IActivitySkuArmory activitySkuArmory;
    private final  IStrategyArmory strategyArmory;
    private final IRaffleStrategy strategyService;
    private final IRaffleActivityPartakeService activityPartakeService;
    private final AwardService awardService;
    private final IBehaviorRebateService rebateService;
    private final IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;
    private final ICreditAdjustService creditAdjustService;
    private final IRaffleActivitySkuProductService raffleActivitySkuProductService;
    public RaffleActivityController(IActivitySkuArmory activitySkuArmory, IStrategyArmory strategyArmory, IRaffleStrategy strategyService, IRaffleActivityPartakeService activityPartakeService, AwardService awardService, IBehaviorRebateService rebateService, IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService, ICreditAdjustService creditAdjustService, IRaffleActivitySkuProductService raffleActivitySkuProductService) {
        this.activitySkuArmory = activitySkuArmory;
        this.strategyArmory = strategyArmory;
        this.strategyService = strategyService;
        this.activityPartakeService = activityPartakeService;
        this.awardService = awardService;
        this.rebateService = rebateService;
        this.raffleActivityAccountQuotaService = raffleActivityAccountQuotaService;
        this.creditAdjustService = creditAdjustService;
        this.raffleActivitySkuProductService = raffleActivitySkuProductService;
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
                    .awardConfig(raffleAwardEntity.getAwardConfig())
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

    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到返利结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate">/api/v1/raffle/activity/calendar_sign_rebate</a>
     * 入参：xiaofuge
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value = "calender_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(@RequestParam String userId) {
        try{
            log.info("日历签到返利开始 userId:{}", userId);
            List<String> orderIds = rebateService.createOrder(BehaviorEntity.builder()
                    .userId(userId)
                    .outBusinessNo(dateFormatDay.format(new Date()))
                    .behaviorType(BehaviorTypeVO.SIGN)
                    .build());
            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        }catch (AppException e) {
            log.error("用户签到失败 userId:{} activityId:{}",userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("用户签到失败 未知错误 userId:{} ",userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }


    /**
     * 判断是否签到接口
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/is_calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value = "is_calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> isCalendarSignRebate(String userId) {
        try{
            log.info("查询用户是否已经签到开始 userId:{}", userId);
            String outBusinessNo = dateFormatDay.format(new Date());
            Integer behaviorRebateOrderSize = rebateService.queryOrderByOutBusinessNo(userId, outBusinessNo);
            log.info("查询用户是否完成日历签到返利完成 userId:{} orders.size:{}", userId, behaviorRebateOrderSize);
            return Response.<Boolean>builder()
                    .data(behaviorRebateOrderSize != 0)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getCode())
                    .build();
        }
        catch (AppException e) {
            log.error("查询用户是否已签到失败 userId:{} activityId:{}",userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询用户是否已签到失败 未知错误 userId:{} activityId:{}",userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /**
     * 查询账户额度
     * <p>
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/activity/query_user_activity_account \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */


    @RequestMapping(value = "query_user_activity_account", method = RequestMethod.POST)
    @Override
    public Response<UserActivityAccountResponseDTO> queryUserActivityAccount(@RequestBody UserActivityAccountRequestDTO request) {
        String userId = request.getUserId();
        Long activityId = request.getActivityId();
        try{
            log.error("查询用户活动账户开始 userId:{} activityId:{}",userId,activityId);
            // 1. 参数校验
            if (StringUtils.isBlank(userId) || null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            RaffleActivityAccountEntity activityAccountEntity = raffleActivityAccountQuotaService.queryActivityAccountEntity(userId,activityId);
            UserActivityAccountResponseDTO responseDTO = getUserActivityAccountResponseDTO(activityAccountEntity);
            log.error("查询用户活动账户完成 userId:{} activityId:{} dto:{}",userId,activityId,JSON.toJSONString(responseDTO));
            return  Response.<UserActivityAccountResponseDTO>builder()
                    .data(responseDTO)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getCode())
                    .build();
        }catch (AppException e) {
            log.error("查询用户账户次数余额失败 userId:{} activityId:{}",userId,activityId, e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询用户账户次数余额失败 未知错误 userId:{} activityId:{}",userId,activityId, e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "credit_pay_exchange_sku", method = RequestMethod.POST)
    @Override
    public Response<Boolean> creditPayExchangeSku(@RequestBody SkuProductShopCartRequestDTO request) {
        try {
            String userId = request.getUserId();
            Long sku = request.getSku();
            log.info("积分兑换商品开始 userId:{} sku:{}",userId, sku);
            if(StringUtils.isBlank(userId) || null == sku) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 1. 创建兑换商品sku订单，outBusinessNo 每次创建出一个单号。
            UnpaidActivityOrderEntity unpaidActivityOrder = raffleActivityAccountQuotaService.createOrder(SkuRechargeEntity.builder()
                    .userId(request.getUserId())
                    .sku(request.getSku())
                    .outBusinessNo(RandomStringUtils.randomNumeric(12))
                    .orderTradeType(OrderTradeTypeVO.credit_pay_trade)
                    .build());
            log.info("积分兑换商品，创建订单完成 userId:{} sku:{} outBusinessNo:{}", request.getUserId(), request.getSku(), unpaidActivityOrder.getOutBusinessNo());

            // 2.支付兑换商品
            String orderId = creditAdjustService.createOrder(TradeEntity.builder()
                    .userId(unpaidActivityOrder.getUserId())
                    .tradeName(TradeNameVO.CONVERT_SKU)
                    .tradeType(TradeTypeVO.reverse)
                    .amount(unpaidActivityOrder.getPayAmount().negate())
                    .outBusinessNo(unpaidActivityOrder.getOutBusinessNo())
                    .build());
            log.info("积分兑换商品，支付订单完成  userId:{} sku:{} orderId:{}", request.getUserId(), request.getSku(), orderId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("积分兑换商品失败 userId:{} sku:{}", request.getUserId(), request.getSku(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    @Override
    @RequestMapping(value = "query_sku_products", method = RequestMethod.POST)
    public Response<List<SkuProductResponseDTO>> querySkuProductListByActivityId(@RequestParam Long activityId) {
        try {
            log.info("查询sku商品集合开始 activityId:{}", activityId);
            // 1. 参数校验
            if (null == activityId) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2. 查询商品&封装数据
            List<SkuProductEntity> skuProductEntities = raffleActivitySkuProductService.querySkuProductEntityListByActivityId(activityId);
            List<SkuProductResponseDTO> skuProductResponseDTOS = getSkuProductResponseDTOS(skuProductEntities);
            log.info("查询sku商品集合完成 activityId:{} skuProductResponseDTOS:{}", activityId, JSON.toJSONString(skuProductResponseDTOS));
            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(skuProductResponseDTOS)
                    .build();
        } catch (Exception e) {
            log.error("查询sku商品集合失败 activityId:{}", activityId, e);
            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private static List<SkuProductResponseDTO> getSkuProductResponseDTOS(List<SkuProductEntity> skuProductEntities) {
        List<SkuProductResponseDTO> skuProductResponseDTOS = new ArrayList<>(skuProductEntities.size());
        for (SkuProductEntity skuProductEntity : skuProductEntities) {
            SkuProductResponseDTO.ActivityCount activityCount = new SkuProductResponseDTO.ActivityCount();
            activityCount.setTotalCount(skuProductEntity.getActivityCount().getTotalCount());
            activityCount.setMonthCount(skuProductEntity.getActivityCount().getMonthCount());
            activityCount.setDayCount(skuProductEntity.getActivityCount().getDayCount());
            SkuProductResponseDTO skuProductResponseDTO = getSkuProductResponseDTO(skuProductEntity, activityCount);
            skuProductResponseDTOS.add(skuProductResponseDTO);
        }
        return skuProductResponseDTOS;
    }

    @Override
    @RequestMapping(value = "query_user_credit", method = RequestMethod.POST)
    public Response<BigDecimal> queryUserCreditAccount(@RequestParam  String userId) {
        try {
            log.info("查询用户积分值开始 userId:{}", userId);
            UserCreditAccountEntity creditAccountEntity = creditAdjustService.queryUserCreditAccount(userId);
            log.info("查询用户积分值完成 userId:{} adjustAmount:{}", userId, creditAccountEntity.getAdjustAmount());
            return Response.<BigDecimal>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(creditAccountEntity.getAdjustAmount())
                    .build();
        } catch (Exception e) {
            return Response.<BigDecimal>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    private static UserActivityAccountResponseDTO getUserActivityAccountResponseDTO(RaffleActivityAccountEntity activityAccountEntity) {
        UserActivityAccountResponseDTO responseDTO = new UserActivityAccountResponseDTO();
        responseDTO.setTotalCount(activityAccountEntity.getTotalCount());
        responseDTO.setTotalCountSurplus(activityAccountEntity.getTotalCountSurplus());
        responseDTO.setMonthCount(activityAccountEntity.getMonthCount());
        responseDTO.setMonthCountSurplus(activityAccountEntity.getMonthCountSurplus());
        responseDTO.setDayCount(activityAccountEntity.getDayCount());
        responseDTO.setDayCountSurplus(activityAccountEntity.getDayCountSurplus());
        return responseDTO;
    }
    private static SkuProductResponseDTO getSkuProductResponseDTO(SkuProductEntity skuProductEntity, SkuProductResponseDTO.ActivityCount activityCount) {
        SkuProductResponseDTO skuProductResponseDTO = new SkuProductResponseDTO();
        skuProductResponseDTO.setSku(skuProductEntity.getSku());
        skuProductResponseDTO.setActivityId(skuProductEntity.getActivityId());
        skuProductResponseDTO.setActivityCountId(skuProductEntity.getActivityCountId());
        skuProductResponseDTO.setStockCount(skuProductEntity.getStockCount());
        skuProductResponseDTO.setStockCountSurplus(skuProductEntity.getStockCountSurplus());
        skuProductResponseDTO.setProductAmount(skuProductEntity.getProductAmount());
        skuProductResponseDTO.setActivityCount(activityCount);
        return skuProductResponseDTO;
    }
}
