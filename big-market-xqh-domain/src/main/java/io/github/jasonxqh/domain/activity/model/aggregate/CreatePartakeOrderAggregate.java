package io.github.jasonxqh.domain.activity.model.aggregate;


import io.github.jasonxqh.domain.activity.model.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePartakeOrderAggregate {

    private String userId;

    private Long activityId;

    /**
     * 活动账户实体
     */
    private RaffleActivityAccountEntity raffleActivityAccountEntity;
    /**
     * 月额度
     */
    private RaffleActivityAccountMonthEntity raffleActivityAccountMonthEntity;

    private boolean isExistAccountMonth = true;
    /**
     * 日额度
     */
    private RaffleActivityAccountDayEntity raffleActivityAccountDayEntity;

    private boolean isExistAccountDay = true;
    /**
     * partake 订单实体
     */
    private UserRaffleOrderEntity userRaffleOrderEntity;
}
