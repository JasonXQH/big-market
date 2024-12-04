package io.github.jasonxqh.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRaffleActivityAccountDayDao {

    void insertActivityAccountDay(RaffleActivityAccountDay rafleActivityAccountDay);

    int updateActivityAccountDaySubstractionQuota(RaffleActivityAccountDay raffleActivityAccountDay);

    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);


    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDayReq);

    int updateActivityAccountDayAddQuota(RaffleActivityAccountDay raffleActivityAccountDay);
}