package io.github.jasonxqh.api;

import io.github.jasonxqh.api.dto.*;
import io.github.jasonxqh.api.response.Response;

import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description :
 **/
public interface IRaffleStrategyService {
    //策略装配接口
    Response<Boolean> strategyArmory(Long strategyId);

    Response<List<RaffleAwardListResponseDTO>> queryStrategyAwardList(RaffleAwardListRequestDTO requestDTO);

    Response<RaffleStrategyResponseDTO> randomRaffle(RaffleStrategyRequestDTO requestDTO);


    /**
     * 查询抽奖策略权重规则，给用户展示出抽奖N次后必中奖奖品范围
     *
     * @param request 请求对象
     * @return 权重奖品配置列表「这里会返回全部，前端可按需展示一条已达标的，或者一条要达标的」
     */
    Response<List<RaffleStrategyRuleWeightResponseDTO>> queryRaffleStrategyRuleWeight(RaffleStrategyRuleWeightRequestDTO request);

}
