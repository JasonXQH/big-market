package io.github.jasonxqh.infrastructure.adapter.repository;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyEntity;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyRuleEntity;
import io.github.jasonxqh.domain.strategy.model.vo.*;
import io.github.jasonxqh.infrastructure.dao.*;
import io.github.jasonxqh.infrastructure.dao.po.strategy.*;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import io.github.jasonxqh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.github.jasonxqh.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

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
        log.info("开始查询awardList");
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;
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
                        .awardTitle(strategyAward.getAwardTitle())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        .sort(strategyAward.getSort())
                        .build())
                .collect(Collectors.toList());
        redisService.setValue(cacheKey, strategyAwardEntities);
        log.info("查询awardList成功");
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
        redisService.setValue(cacheKey, strategyEntity);
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
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String key) {
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
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

    @Override
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public Boolean subtractAwardStock(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if(surplus < 0){
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        Boolean lock = redisService.setNx(lockKey);
        if(!lock) log.info("策略奖品库存加锁失败 {}",lockKey);
        return lock;
    }


    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<Object> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<Object> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO,3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
//        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(destinationQueue);
        //看看会发生什么？
        return destinationQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + Constants.UNDERLINE + awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if (null != strategyAwardEntity) return strategyAwardEntity;

        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        StrategyAward award = strategyAwardDao.queryStrategyAward(strategyAward);
        strategyAwardEntity = StrategyAwardEntity.builder()
                  .strategyId(award.getStrategyId())
                  .awardId(award.getAwardId())
                  .awardTitle(award.getAwardTitle())
                  .awardSubtitle(award.getAwardSubtitle())
                  .awardCount(award.getAwardCount())
                  .awardCountSurplus(award.getAwardCountSurplus())
                  .awardRate(award.getAwardRate())
                  .sort(award.getSort())
                  .build();
        // 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);
        return strategyAwardEntity;
    }

}
