package io.github.jasonxqh.domain.strategy.service.armory;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyRuleEntity;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.enums.ResponseCode;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description :策略装备库，负责初始化策略计算
 **/

@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory,IStrategyDispatch {
    @Resource
    private IStrategyRepository repository;

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        //1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities =  repository.queryStrategyAwardList(strategyId);
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);
        //2. 缓存奖品库存，用于decr扣减库存使用
        for(StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            Integer awardCount = strategyAwardEntity.getAwardCount();
            cacheStrategyCount(strategyId,awardId,awardCount);

        }
        //3.1 默认装配配置[全量抽奖概率]
        //3.2 权重策略配置，适用于rule_weight权重规则配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleModel = strategyEntity.getRuleWeight();
        if(null == ruleModel) return true;

        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRule(strategyId, ruleModel);

        if(null == strategyRuleEntity){
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(),ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo()  );
        };
        Map<String, List<Integer>>  ruleWeightsValueMap = strategyRuleEntity.getRuleWeights();

        Set<String> keys = ruleWeightsValueMap.keySet();
        for(String key : keys){
            List<Integer>ruleWeightValues = ruleWeightsValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntitiesClone);
        }


        return true;
    }

    private void cacheStrategyCount(Long strategyId, Integer awardId, Integer awardCount) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY  +strategyId + "_" + awardId;
        repository.cacheStrategyAwardCount(cacheKey,awardCount);
    }

    //专门做装配动作
    private void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities ){
        //1. 读取表中数据的加和值，以及最小概率值。
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        //2.获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //3.用总和除以最小值，获取概率范围。(百分位千分位万分位)
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate,0,BigDecimal.ROUND_CEILING);
        //4. 生成策略
        ArrayList<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for(StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();

            //计算出每个概率值需要存放到查找表中的数量，循环填充
            for(int i = 0;i< rateRange.multiply(awardRate).setScale(0,BigDecimal.ROUND_CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }

        //5. shuffle
        Collections.shuffle(strategyAwardSearchRateTables);

        //6. 生成集合
        HashMap<Integer,Integer> shuffleStrategyAwardSearchRateTables = new HashMap<>();
        for(int i = 0;i< strategyAwardSearchRateTables.size();i++) {
            shuffleStrategyAwardSearchRateTables.put(i,strategyAwardSearchRateTables.get(i));
        }

        //7. 存储到redis
        repository.storeStrategyAwardSearchRateTables(key,shuffleStrategyAwardSearchRateTables.size(),shuffleStrategyAwardSearchRateTables);

    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        int rateRange = repository.getRateRange(strategyId);
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId),new SecureRandom().nextInt(rateRange
        ));
    }


    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        int rateRange = repository.getRateRange(key);
        return repository.getStrategyAwardAssemble(key,new SecureRandom().nextInt(rateRange
        ));
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY+strategyId + "_" + awardId;
        return repository.subtractAwardStock(cacheKey);

    }
}
