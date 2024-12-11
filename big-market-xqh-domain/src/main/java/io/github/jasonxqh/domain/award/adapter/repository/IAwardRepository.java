package io.github.jasonxqh.domain.award.adapter.repository;

import io.github.jasonxqh.domain.award.model.aggregate.GiveOutPrizesAggregate;
import io.github.jasonxqh.domain.award.model.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {


    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

    String queryAwardConfigByAwardId(Integer awardId);

    String queryAwardKeyByAwardId(Integer awardId);
    void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate);
}
