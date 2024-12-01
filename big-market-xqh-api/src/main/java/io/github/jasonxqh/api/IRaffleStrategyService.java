package io.github.jasonxqh.api;

import io.github.jasonxqh.api.dto.RaffleAwardListRequestDTO;
import io.github.jasonxqh.api.dto.RaffleAwardListResponseDTO;
import io.github.jasonxqh.api.dto.RaffleStrategyRequestDTO;
import io.github.jasonxqh.api.dto.RaffleStrategyResponseDTO;
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
}
