package io.github.jasonxqh.domain.strategy.service.armory;

import io.github.jasonxqh.domain.strategy.adapter.repository.IStrategyRepository;
import io.github.jasonxqh.domain.strategy.model.entity.StrategyAwardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description :策略装备库，负责初始化策略计算
 **/

@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory {
    @Resource
    private IStrategyRepository repository;

    @Override
    public void assembleLotteryStrategy(Long strategyId) {
        //1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities =  repository.queryStrategyAwardList(strategyId);

        //2. 读取表中数据的加和值，以及最小概率值。
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        //3.获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //4.用综合除以最小值，获取概率范围。(百分位千分位万分位)
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate,0,BigDecimal.ROUND_CEILING);
        //5. 生成策略
        ArrayList<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for(StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();

            //计算出每个概率值需要存放到查找表中的数量，循环填充
            for(int i = 0;i< rateRange.multiply(awardRate).setScale(0,BigDecimal.ROUND_CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }

        //6. shuffle
        Collections.shuffle(strategyAwardSearchRateTables);

        //7. 生成集合
        HashMap<Integer,Integer> shuffleStrategyAwardSearchRateTables = new HashMap<>();
        for(int i = 0;i< strategyAwardSearchRateTables.size();i++) {
            shuffleStrategyAwardSearchRateTables.put(i,strategyAwardSearchRateTables.get(i));
        }

        //8. 存储到redis
        repository.storeStrategyAwardSearchRateTables(strategyId,shuffleStrategyAwardSearchRateTables.size(),shuffleStrategyAwardSearchRateTables);

    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        int rateRange = repository.getRateRange(strategyId);
        return repository.getStrategyAwardAssemble(strategyId,new SecureRandom().nextInt(rateRange
        ));
    }
}
