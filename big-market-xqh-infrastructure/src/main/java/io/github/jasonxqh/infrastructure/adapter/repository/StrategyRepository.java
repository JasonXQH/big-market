package io.github.jasonxqh.infrastructure.adapter.repository;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyRuleEntity;
import io.github.jasonxqh.infrastructure.dao.IStrategyAwardDao;
import io.github.jasonxqh.infrastructure.dao.IStrategyDao;
import io.github.jasonxqh.infrastructure.dao.IStrategyRuleDao;
import io.github.jasonxqh.infrastructure.dao.po.Strategy;
import io.github.jasonxqh.infrastructure.dao.po.StrategyAward;
import io.github.jasonxqh.infrastructure.dao.po.StrategyRule;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 策略仓储实现
 **/
@Repository
public class StrategyRepository implements IStrategyRepository {
    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IStrategyDao strategyDao;


    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Resource
    private IRedisService redisService;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        if(strategyAwardEntities != null && !strategyAwardEntities.isEmpty()) {
            return strategyAwardEntities;
        }
        // 从库中读取数据
        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        //转换成StrategyAward
        strategyAwardEntities = strategyAwards.stream()
                .map(strategyAward -> StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .build())
                .collect(Collectors.toList());
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_KEY+strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if(strategyEntity != null) { return strategyEntity; }

        Strategy strategy = strategyDao.queryStrategyRuleModels(strategyId);

         strategyEntity = StrategyEntity.builder()
                  .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                 .ruleModels(strategy.getRuleModels())
                  .build();
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel){
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRule = strategyRuleDao.queryStrategyRule(strategyRuleReq);
         StrategyRuleEntity strategyRuleEntity = StrategyRuleEntity.builder()
                  .strategyId(strategyRule.getStrategyId())
                  .awardId(strategyRule.getAwardId())
                  .ruleType(strategyRule.getRuleType())
                  .ruleModel(strategyRule.getRuleModel())
                  .ruleValue(strategyRule.getRuleValue())
                  .build();
        return strategyRuleEntity;
    }

    @Override
    public void storeStrategyAwardSearchRateTables(String key, int rateRange, HashMap<Integer, Integer> shuffleStrategyAwardSearchRateTables) {
        //存储rateRange, 如10000
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY+key, rateRange);
        //存储概率查找表
        RMap<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY +strategyId);
    }

    @Override
    public int getRateRange(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }


    @Override
    public Integer getStrategyAwardAssemble(String key, int i) {
        return (Integer) redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY+key,i);
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        strategyRuleReq.setAwardId(awardId);
        return strategyRuleDao.queryStrategyRuleValue(strategyRuleReq);
    }


}
