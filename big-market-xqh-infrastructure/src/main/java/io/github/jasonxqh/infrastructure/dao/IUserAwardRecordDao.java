package io.github.jasonxqh.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import io.github.jasonxqh.infrastructure.dao.po.award.UserAwardRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserAwardRecordDao {
    void saveUserAwardRecord(UserAwardRecord record);
}