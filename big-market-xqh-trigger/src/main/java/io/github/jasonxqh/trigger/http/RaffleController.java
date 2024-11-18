package io.github.jasonxqh.trigger.http;

import com.alibaba.fastjson.JSON;
import io.github.jasonxqh.api.IRaffleService;
import io.github.jasonxqh.api.dto.RaffleAwardListRequestDTO;
import io.github.jasonxqh.api.dto.RaffleAwardListResponseDTO;
import io.github.jasonxqh.api.dto.RaffleRequestDTO;
import io.github.jasonxqh.api.dto.RaffleResponseDTO;
import io.github.jasonxqh.api.response.Response;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.service.IRaffleAward;
import io.github.jasonxqh.domain.strategy.service.IRaffleStrategy;
import io.github.jasonxqh.domain.strategy.service.armory.IStrategyArmory;
import io.github.jasonxqh.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/v1/raffle/")
public class RaffleController implements IRaffleService {
    private IRaffleStrategy raffleStrategy;
    private IStrategyArmory strategyArmory;
    private IRaffleAward raffleAward;
    public RaffleController(IRaffleStrategy raffleStrategy, IStrategyArmory strategyArmory, IRaffleAward raffleAward) {
        this.raffleStrategy = raffleStrategy;
        this.strategyArmory = strategyArmory;
        this.raffleAward = raffleAward;
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
    public Response<List<RaffleAwardListResponseDTO>> queryStrategyAwardList(@RequestBody RaffleAwardListRequestDTO requestDTO) {
        Long strategyId = requestDTO.getStrategyId();
        try {
            log.info("查询抽奖奖品列表开始 strategyId:{}", strategyId);
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardList(strategyId);
            List<RaffleAwardListResponseDTO> awardListResponseDTOS = strategyAwardEntities.stream()
                    .map(award -> RaffleAwardListResponseDTO.builder()
                            .awardId(award.getAwardId())
                            .awardTitle(award.getAwardTitle())
                            .awardSubtitle(award.getAwardSubtitle())
                            .build())
                    .collect(Collectors.toList());

            Response<List<RaffleAwardListResponseDTO>> response = Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(awardListResponseDTOS)
                    .build();
            log.info("查询奖品列表成功 strategyId：{} response: {}", strategyId, JSON.toJSONString(response));

        } catch (Exception e) {
//            throw new RuntimeException(e);
            log.info("查询奖品列表失败 strategyId：{}", strategyId);

            return  Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
        return null;

    }
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleResponseDTO> randomRaffle(@RequestBody RaffleRequestDTO requestDTO) {
        try{
            Long strategyId = requestDTO.getStrategyId();
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .strategyId(strategyId)
                    .userId("system")
                    .build());

            Response<RaffleResponseDTO> response = Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
//            throw new RuntimeException(e);
            log.info("随机抽奖失败 strategyId: {} response: {}", requestDTO.getStrategyId());
            return Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
