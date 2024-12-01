package io.github.jasonxqh.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import io.github.jasonxqh.infrastructure.dao.po.strategy.UserRaffleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {
    @DBRouter
    UserRaffleOrder queryUnusedUserRaffleOrder(UserRaffleOrder userRaffleOrder);

    int insertUserRaffleOrder(UserRaffleOrder userRaffleOrder);

    int updateUserRaffleOrderStateUsed(UserRaffleOrder userRaffleOrderReq);
}