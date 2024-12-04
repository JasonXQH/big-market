package io.github.jasonxqh.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountDao {
    int updateAccountQuota(RaffleActivityAccount rafleActivityAccount);

    void insert(RaffleActivityAccount raffleActivityAccount);

    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserIdAndActivityId(RaffleActivityAccount account);

    int updateActivityAccountSubstractionQuota(RaffleActivityAccount build);

    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount build);

    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount build);
}