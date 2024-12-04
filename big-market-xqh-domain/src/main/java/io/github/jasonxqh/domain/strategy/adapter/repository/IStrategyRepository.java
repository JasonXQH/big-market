package io.github.jasonxqh.domain.strategy.adapter.repository;

import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyRuleEntity;
import io.github.jasonxqh.domain.strategy.model.vo.RuleTreeVO;
import io.github.jasonxqh.domain.strategy.model.vo.RuleWeightVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardRuleModelVO;
import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 仓储策略 接口
 **/
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel);

    void storeStrategyAwardSearchRateTables(String key, int rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables);

    int getRateRange(Long strategyId);

    int getRateRange(String key);

    Integer getStrategyAwardAssemble(String key, int i);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);
    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    void cacheStrategyAwardCount(String cacheKey, Integer awardCount);

    Boolean subtractAwardStock(StrategyAwardEntity strategyAwardEntity, Date endDateTime);
    Boolean subtractAwardStock(StrategyAwardEntity strategyAwardEntity);

    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    List<StrategyAwardStockKeyVO> takeQueueValue();

//    StrategyAwardStockKeyVO takeQueueValue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    void updateStrategyAwardStock(Long strategyId, Integer awardId);

    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);

    void clearStrategyAwardStock(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    void clearQueueValue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    Long queryStrategyIdByActivityId(Long activityId);

    Integer queryTodayUserRaffleCount(String userId, Long strategyId);


    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);


    List<RuleWeightVO> queryAwardRuleWeight(Long strategyId);

    Integer queryActivityAccountTotalUseCount(String userId, Long strategyId);
}
