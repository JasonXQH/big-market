package io.github.jasonxqh.domain.award.adapter.repository;

import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {


    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);
}
