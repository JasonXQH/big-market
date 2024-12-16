package io.github.jasonxqh.infrastructure.dao;
import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import io.github.jasonxqh.domain.activity.model.entity.PartakeRaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.UserRaffleOrderEntity;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author jasonxu
 * @date 2024/11/22
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRaffleActivityOrderDao {

    int insert(RaffleActivityOrder raffleActivityOrder);

    @DBRouter
    List<RaffleActivityOrder> queryRaffleActivityOrderByUserId(String userId);
    @DBRouter
    RaffleActivityOrder queryRaffleActivityOrderByUserIdAndOutBN(RaffleActivityOrder raffleActivityOrderReq);

    int updateOrderCompleted(RaffleActivityOrder raffleActivityOrderReq);

    @DBRouter
    RaffleActivityOrder queryUnpaidActivityOrder(RaffleActivityOrder raffleActivityOrderReq);
}
