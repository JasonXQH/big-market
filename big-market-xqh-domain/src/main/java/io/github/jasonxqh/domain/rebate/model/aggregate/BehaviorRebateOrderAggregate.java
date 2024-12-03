package io.github.jasonxqh.domain.rebate.model.aggregate;

import io.github.jasonxqh.domain.rebate.model.entity.TaskEntity;
import io.github.jasonxqh.domain.rebate.model.entity.UserBehaviorRebateOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateOrderAggregate {
    private UserBehaviorRebateOrderEntity userBehaviorRebateOrder;
    private TaskEntity taskEntity;
}
