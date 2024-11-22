package io.github.jasonxqh.domain.activity.model.aggregate;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityAccountActivity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {

    /**
     * 活动账户实体
     */
    private RaffleActivityAccountActivity raffleActivityAccountActivity;
    /**
     * 活动订单实体
     */
    private RaffleActivityOrderEntity raffleActivityOrderEntity;

}
