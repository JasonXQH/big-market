package io.github.jasonxqh.infrastructure.adapter.repository;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyRuleEntity;
import io.github.jasonxqh.domain.strategy.model.vo.*;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.*;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 策略仓储实现
 **/
@Slf4j
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


    @Resource
    private IRuleTreeDao ruleTreeDao;

    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;

    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;

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

    @Override
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        return queryStrategyRuleValue(strategyId, null, ruleModel);
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);

        String ruleModels = strategyAwardDao.queryStrategyAwardRuleModel(strategyAwardReq);
        return StrategyAwardRuleModelVO.builder()
                .ruleModels(ruleModels)
                .build();
    }


    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId){
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if (null != ruleTreeVOCache) return ruleTreeVOCache;
        log.info("进入queryRuleTreeVOByTreeId, treeId: {}", treeId);
        // 从数据库获取
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);

        //构造 RuleTreeVO 基本信息
         RuleTreeVO ruleTreeVO = RuleTreeVO.builder()
                  .treeId(ruleTree.getTreeId())
                  .treeName(ruleTree.getTreeName())
                  .treeDesc(ruleTree.getTreeDesc())
                 .treeRootRuleNode(ruleTree.getTreeNodeRuleKey())
                 .treeNodeMap(new HashMap<>())
                  .build();

        //构造节点连线映射，便于后续给节点添加连线：
        Map<String, List<RuleTreeNodeLine>> nodeLineMap = ruleTreeNodeLines.stream()
                .collect(Collectors.groupingBy(RuleTreeNodeLine::getRuleNodeFrom));


        //构造RuleTreeNodeVO,并添加到treeNodeMap中
        for(RuleTreeNode node : ruleTreeNodes) {
            List<RuleTreeNodeLineVO> lineVOList= Optional.ofNullable(nodeLineMap.get(node.getRuleKey()))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(line ->  RuleTreeNodeLineVO.builder()
                            .treeId(line.getTreeId())
                            .ruleNodeFrom(line.getRuleNodeFrom())
                            .ruleNodeTo(line.getRuleNodeTo())
                            .ruleLimitType(RuleLimitTypeVO.valueOf(line.getRuleLimitType()))
                            .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(line.getRuleLimitValue()))
                            .build())
                    .collect(Collectors.toList());
            RuleTreeNodeVO nodeVO = RuleTreeNodeVO.builder()
                    .ruleValue(node.getRuleValue())
                    .ruleKey(node.getRuleKey())
                    .treeId(node.getTreeId())
                    .ruleDesc(node.getRuleDesc())
                    .treeNodeLineVOList(lineVOList)
                    .build();
            ruleTreeVO.getTreeNodeMap().put(node.getRuleKey(), nodeVO);
        }
        redisService.setValue(cacheKey, ruleTreeVO);
        return ruleTreeVO;
    }



}
