package io.github.jasonxqh.domain.activity.model.aggregate;

import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityAccountEntity;
import io.github.jasonxqh.domain.activity.model.entity.RaffleActivityOrderEntity;
import io.github.jasonxqh.domain.activity.model.valobj.OrderStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSkuQuotaOrderAggregate {

    /**
     * 活动账户实体
     */
    private RaffleActivityAccountEntity raffleActivityAccountEntity;
    /**
     * 活动订单实体
     */
    private RaffleActivityOrderEntity raffleActivityOrderEntity;

    public void setOrderState(OrderStateVO orderState) {
        this.raffleActivityOrderEntity.setState(orderState);
    }

    public void setPayAmount(BigDecimal zero) {
        this.raffleActivityOrderEntity.setPayAmount(zero);
    }
}
