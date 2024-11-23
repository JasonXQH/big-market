package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountDao {
    int updateAccountQuota(RaffleActivityAccount rafleActivityAccount);

    void insert(RaffleActivityAccount raffleActivityAccount);
}