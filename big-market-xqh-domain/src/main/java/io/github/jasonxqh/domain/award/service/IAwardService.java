package io.github.jasonxqh.domain.award.service;

import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;

public interface IAwardService {
    //记录流水和task表
    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);
}
