package io.github.jasonxqh.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import io.github.jasonxqh.domain.activity.model.entity.PartakeRaffleActivityEntity;
import io.github.jasonxqh.domain.activity.model.entity.UserRaffleOrderEntity;
import io.github.jasonxqh.infrastructure.dao.po.strategy.UserRaffleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {
    @DBRouter
    UserRaffleOrderEntity queryUnusedUserRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    int insertUserRaffleOrder(UserRaffleOrder userRaffleOrder);
}