package io.github.jasonxqh.infrastructure.adapter.support;

import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.infrastructure.redis.IRedisService;
import io.github.jasonxqh.types.common.Constants;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueueManager {

    private final Map<String, RBlockingQueue<StrategyAwardStockKeyVO>> rBlockingQueueMap = new ConcurrentHashMap<>();
    private final Map<String, RDelayedQueue<StrategyAwardStockKeyVO>> rDelayedQueueMap = new ConcurrentHashMap<>();

    @Resource
    private IRedisService redisService;

    public RBlockingQueue<StrategyAwardStockKeyVO> getOrCreateRBlockingQueue( StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String queueKey = strategyAwardStockKeyVO.getStrategyId() + "_" + strategyAwardStockKeyVO.getAwardId();
        return rBlockingQueueMap.computeIfAbsent(queueKey, key -> {
            String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY + queueKey;
            return redisService.getBlockingQueue(cacheKey);
        });
    }

    public RDelayedQueue<StrategyAwardStockKeyVO> getOrCreateRDelayedQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String queueKey = strategyAwardStockKeyVO.getStrategyId() + "_" + strategyAwardStockKeyVO.getAwardId();
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY + queueKey;
        rBlockingQueueMap.computeIfAbsent(cacheKey, key -> {
            return redisService.getBlockingQueue(cacheKey);
        });
        return rDelayedQueueMap.computeIfAbsent(cacheKey, key -> {
            RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = rBlockingQueueMap.get(cacheKey);
            return redisService.getDelayedQueue(blockingQueue);
        });
    }

    public Map<String, RBlockingQueue<StrategyAwardStockKeyVO>> getAllRBlockingQueues() {
        return rBlockingQueueMap;
    }
}
