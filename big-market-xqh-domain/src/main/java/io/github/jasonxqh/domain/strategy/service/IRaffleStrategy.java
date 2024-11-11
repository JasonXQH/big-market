package io.github.jasonxqh.domain.strategy.service;

import io.github.jasonxqh.domain.strategy.model.entity.RaffleAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.RaffleFactorEntity;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/11, 星期一
 * @Description : 执行抽奖；用抽奖因子入参，执行抽奖计算，返回奖品信息
 **/


public interface IRaffleStrategy {

     RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntityEntity);
}
