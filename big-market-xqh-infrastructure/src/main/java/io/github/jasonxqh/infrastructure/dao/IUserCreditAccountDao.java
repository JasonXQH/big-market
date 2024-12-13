package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.award.UserCreditAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserCreditAccountDao {
    int updateAddAmount(UserCreditAccount userCreditAccountReq);

    void insert(UserCreditAccount userCreditAccountReq);

    UserCreditAccount queryUserCreditAccountByUserId(UserCreditAccount userCreditAccountReq);

    void updateSubstractAmount(UserCreditAccount userCreditAccountReq);

    //对积分账户的操作
}