package io.github.jasonxqh.infrastructure.adapter.support;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
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

    private final Map<String, RBlockingQueue<StrategyAwardStockKeyVO>> rBlockingStrategyAwardStockKeyVOQueueMap = new ConcurrentHashMap<>();
    private final Map<String, RDelayedQueue<StrategyAwardStockKeyVO>> rDelayedStrategyAwardStockKeyVOQueueMap = new ConcurrentHashMap<>();

    private final Map<String, RBlockingQueue<ActivitySkuStockKeyVO>> rBlockingActivitySkuStockKeyVOQueueMap = new ConcurrentHashMap<>();
    private final Map<String, RDelayedQueue<ActivitySkuStockKeyVO>> rDelayedActivitySkuStockKeyVOQueueMap = new ConcurrentHashMap<>();



    @Resource
    private IRedisService redisService;

    public RDelayedQueue<ActivitySkuStockKeyVO> getOrCreateActivitySkuStockKeyVORDelayedQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String queueKey = activitySkuStockKeyVO.getActivityId() + "_" + activitySkuStockKeyVO.getSku();
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY + queueKey;
        rBlockingActivitySkuStockKeyVOQueueMap.computeIfAbsent(cacheKey, key -> redisService.getBlockingQueue(cacheKey));
        return rDelayedActivitySkuStockKeyVOQueueMap.computeIfAbsent(cacheKey, key -> {
            RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = rBlockingActivitySkuStockKeyVOQueueMap.get(cacheKey);
            return redisService.getDelayedQueue(blockingQueue);
        });
    }

    public RDelayedQueue<StrategyAwardStockKeyVO> getOrCreateStrategyAwardStockKeyVORDelayedQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String queueKey = strategyAwardStockKeyVO.getStrategyId() + "_" + strategyAwardStockKeyVO.getAwardId();
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY + queueKey;
        rBlockingStrategyAwardStockKeyVOQueueMap.computeIfAbsent(cacheKey, key -> redisService.getBlockingQueue(cacheKey));
        return rDelayedStrategyAwardStockKeyVOQueueMap.computeIfAbsent(cacheKey, key -> {
            RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = rBlockingStrategyAwardStockKeyVOQueueMap.get(cacheKey);
            return redisService.getDelayedQueue(blockingQueue);
        });
    }


    public Map<String, RBlockingQueue<StrategyAwardStockKeyVO>> getAllStrategyAwardStockKeyVORBlockingQueues() {
        return rBlockingStrategyAwardStockKeyVOQueueMap;
    }
    public Map<String, RBlockingQueue<ActivitySkuStockKeyVO>> getAllActivitySkuStockKeyVORBlockingQueues() {
        return rBlockingActivitySkuStockKeyVOQueueMap;
    }
}
