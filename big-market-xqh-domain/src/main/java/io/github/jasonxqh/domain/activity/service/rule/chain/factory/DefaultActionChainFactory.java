package io.github.jasonxqh.domain.activity.service.rule.chain.factory;

import io.github.jasonxqh.domain.activity.service.rule.chain.IActionChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class DefaultActionChainFactory {


    private final IActionChain actionChain;
    public DefaultActionChainFactory(Map<String, IActionChain> actionChainGroup){
        actionChain = actionChainGroup.get(ActionModel.ACTIVITY_BASE_ACTION.getCode());
        actionChain.appendNext(actionChainGroup.get(ActionModel.ACTIVITY_SKU_STOCK_ACTION.getCode()));
    }


    public IActionChain openLogicChain() {

        return this.actionChain;
    }

    @Getter
    @AllArgsConstructor
    public enum ActionModel {

        ACTIVITY_BASE_ACTION("activity_base_action", "基础校验"),
        ACTIVITY_SKU_STOCK_ACTION("activity_sku_stock_action", "sku校验"),
        ACTIVITY_STATE_ACTION("activity_state_action", "活动状态校验"),
        ACTIVITY_TIME_ACTION("activity_time_action", "活动时间校验"),
        ;

        private final String code;
        private final String info;

    }
}
