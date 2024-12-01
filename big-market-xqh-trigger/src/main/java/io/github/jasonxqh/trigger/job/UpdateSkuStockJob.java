package io.github.jasonxqh.trigger.job;

import io.github.jasonxqh.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import io.github.jasonxqh.domain.activity.service.IRaffleActivitySkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/14, 星期四
 * @Description : 更新奖品库存任务，为了不让更新库存的压力打到数据库中，采用了redis更新缓存库存，异步队列更新数据库，数据库表最终
 * 一致即可
 **/

@Slf4j
@Component()
public class UpdateSkuStockJob {

    @Resource
    private IRaffleActivitySkuStockService skuStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void exec(){
        try {
            log.info("定时任务，更新 sku 消耗库存[延迟队列获取，降低对数据库的更新频次，不要产生竞争");
            ActivitySkuStockKeyVO activitySkuStockKeyVO = skuStock.takeQueueValue();
            if(null == activitySkuStockKeyVO){
                return;
            }
            log.info("定时任务，更新 sku消耗库存 sku:{} activityId:{} ", activitySkuStockKeyVO.getSku(), activitySkuStockKeyVO.getActivityId());
            skuStock.updateStrategyAwardStock(activitySkuStockKeyVO.getSku());
        }catch (Exception e){
            log.error("定时任务，更新sku 消耗库存失败",e);
        }
    }
}