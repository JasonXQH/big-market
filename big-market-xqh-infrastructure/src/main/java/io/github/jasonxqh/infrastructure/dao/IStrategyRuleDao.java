package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.StrategyRule;
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
}
