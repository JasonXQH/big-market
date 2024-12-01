package io.github.jasonxqh.domain.activity.service;


import io.github.jasonxqh.domain.activity.model.entity.PartakeRaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.UserRaffleOrderEntity;

/**
 * @author jasonxu
 * @date 2024/11/26
 * @description 抽奖活动参与服务
 */
public interface IRaffleActivityPartakeService {

    /**
     * 抽奖活动参与服务，扣减活动库存，生成抽奖单
     *
     * @param partakeRaffleActivityEntity
     * @return {@link UserRaffleOrderEntity }
     */
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    UserRaffleOrderEntity createOrder(Long activityId,String userId);
}
