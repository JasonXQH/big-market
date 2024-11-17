package io.github.jasonxqh.api;

import io.github.jasonxqh.api.dto.RaffleAwardListRequestDTO;
import io.github.jasonxqh.api.dto.RaffleAwardListResponseDTO;
import io.github.jasonxqh.api.dto.RaffleRequestDTO;
import io.github.jasonxqh.api.dto.RaffleResponseDTO;
import io.github.jasonxqh.api.response.Response;

import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description :
 **/
public interface IRaffleService {
    Response<Boolean> strategyArmory(Long strategyId);
    Response<List<RaffleAwardListResponseDTO>> queryStrategyAwardList(RaffleAwardListRequestDTO requestDTO);
    Response<RaffleResponseDTO> randomRaffle(RaffleRequestDTO requestDTO);
}
