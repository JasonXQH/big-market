package io.github.jasonxqh.trigger.http;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.api.IRaffleStrategyService;
import io.github.jasonxqh.api.dto.*;
import io.github.jasonxqh.api.response.Response;
import io.github.jasonxqh.domain.activity.service.IRaffleActivityAccountQuotaService;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.service.IRaffleAward;
import io.github.jasonxqh.domain.strategy.service.IRaffleRule;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/v1/raffle/strategy")
public class RaffleStrategyController implements IRaffleStrategyService {
    private final IRaffleStrategy raffleStrategy;
    private final IStrategyArmory strategyArmory;
    private final IRaffleAward raffleAward;
    private final IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;
    private final IRaffleRule raffleRule;

    public RaffleStrategyController(IRaffleStrategy raffleStrategy, IStrategyArmory strategyArmory, IRaffleAward raffleAward, IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService, IRaffleRule raffleRule) {
        this.raffleStrategy = raffleStrategy;
        this.strategyArmory = strategyArmory;
        this.raffleAward = raffleAward;
        this.raffleActivityAccountQuotaService = raffleActivityAccountQuotaService;
        this.raffleRule = raffleRule;
    }

    /**
     * 策略装配，将策略信息装配到缓存中
     * <a href="http://localhost:8091/api/v1/raffle/strategy_armory">/api/v1/raffle/strategy_armory</a>
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    @RequestMapping(value = "strategy_armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {
        try{
            log.info("抽奖策略装配开始 strategyId：{}", strategyId);
            boolean armoryStatus = strategyArmory.assembleLotteryStrategy(strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(armoryStatus)
                    .build();
            log.info("抽奖策略装配完成 strategyId：{} response: {}", strategyId, JSON.toJSONString(response));
            return response;
        }catch (Exception e){
            log.info("抽奖策略装配失败 strategyId：{} response: {}", strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
            return response;
        }
    }
    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleAwardListResponseDTO>> queryStrategyAwardList(@RequestBody RaffleAwardListRequestDTO request) {
        String userId = request.getUserId();
        Long activityId = request.getActivityId();
        try {
            if(StringUtils.isBlank(userId)||activityId==null ){
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            log.info("查询抽奖奖品列表开始 userId:{} activityId:{}", userId , activityId);
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardListByActivityId(activityId);

            // 3. 获取规则配置
            String[] treeIds = strategyAwardEntities.stream()
                    .map(StrategyAwardEntity::getRuleModels)
                    .filter(ruleModel -> ruleModel != null && !ruleModel.isEmpty())
                    .toArray(String[]::new);
            Map<String, Integer> treeIdsRuleLockMap = raffleRule.queryAwardRuleLockCount(treeIds);
            Integer partakeCount = raffleActivityAccountQuotaService.queryRaffleActivityAccountDayPartakeCount(activityId, userId);

            List<RaffleAwardListResponseDTO> awardListResponseDTOS = strategyAwardEntities.stream()
                    .map(strategyAwardEntity -> {
                        Integer awardLockCount = treeIdsRuleLockMap.get(strategyAwardEntity.getRuleModels());
                        RaffleAwardListResponseDTO awardListResponseDTO = new RaffleAwardListResponseDTO();
                        awardListResponseDTO.setAwardId(strategyAwardEntity.getAwardId());
                        awardListResponseDTO.setAwardTitle(strategyAwardEntity.getAwardTitle());
                        awardListResponseDTO.setAwardSubtitle(strategyAwardEntity.getAwardSubtitle());
                        Integer ruleLockCount = treeIdsRuleLockMap.get(strategyAwardEntity.getRuleModels());
                        awardListResponseDTO.setAwardRuleLockCount(ruleLockCount);
                        if (ruleLockCount != null) {
                            Boolean isUnlock = partakeCount >= ruleLockCount;
                            awardListResponseDTO.setIsAwardUnlock(isUnlock);
                            awardListResponseDTO.setWaitUnlockCount(isUnlock ? 0 : ruleLockCount - partakeCount);
                        } else {
                            awardListResponseDTO.setIsAwardUnlock(true);
                            awardListResponseDTO.setWaitUnlockCount(0);
                        }
                         return awardListResponseDTO;
                    })
                    .collect(Collectors.toList());
            Response<List<RaffleAwardListResponseDTO>> response = Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(awardListResponseDTOS)
                    .build();
            log.info("查询奖品列表成功 userId:{} activityId：{} response: {}", userId,activityId, JSON.toJSONString(response));
            return response;
        }
        catch (AppException e) {
            log.error("查询奖品列表失败 userId:{} activityId:{}", userId, activityId, e);
            return Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }
        catch (Exception e) {
            log.info("查询奖品列表失败,未知错误 userId:{} activityId:{}", userId, activityId, e);
            return  Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleStrategyResponseDTO> randomRaffle(@RequestBody RaffleStrategyRequestDTO requestDTO) {
        try{
            Long strategyId = requestDTO.getStrategyId();
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .strategyId(strategyId)
                    .userId("system")
                    .build());
            Response<RaffleStrategyResponseDTO> response = Response.<RaffleStrategyResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleStrategyResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
//            throw new RuntimeException(e);
            log.info("随机抽奖失败 strategyId: {} response: {}", requestDTO.getStrategyId());
            return Response.<RaffleStrategyResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
