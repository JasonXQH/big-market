package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.strategy.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description: 策略规则Dao
 **/
@Mapper
public interface IStrategyRuleDao {
    List<StrategyRule> queryStrategyRuleList();

    StrategyRule queryStrategyRuleByStrategyIdAndRuleModel(StrategyRule strategyRuleReq);

    String queryStrategyRuleValue(StrategyRule strategyRuleReq);

    String queryStrategyRuleByStrategyId(StrategyRule strategyRuleReq);
}

