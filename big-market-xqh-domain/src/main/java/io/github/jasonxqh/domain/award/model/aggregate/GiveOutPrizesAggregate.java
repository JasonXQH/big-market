package io.github.jasonxqh.domain.award.model.aggregate;


import io.github.jasonxqh.domain.award.model.entity.UserAwardRecordEntity;
import io.github.jasonxqh.domain.award.model.entity.UserCreditRandomAwardEntity;
import io.github.jasonxqh.domain.award.model.vo.AwardStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiveOutPrizesAggregate {
    private String userId;

    private UserAwardRecordEntity userAwardRecordEntity;

    private UserCreditRandomAwardEntity userCreditRandomAwardEntity;

    public static UserAwardRecordEntity buildDeliverUserAwardRecordEntity(String userId, String orderId, Integer awardId, AwardStateVO awardStateVO) {
        return UserAwardRecordEntity.builder()
                .userId(userId)
                .orderId(orderId)
                .awardId(awardId)
                .awardState(awardStateVO)
                .build();

    }

    public static UserCreditRandomAwardEntity buildUserCreditRandomAwardEntity(String userId, BigDecimal creditAmount){
        return  UserCreditRandomAwardEntity.builder()
                .userId(userId)
                .creditAmount(creditAmount)
                .build();
    }

}
