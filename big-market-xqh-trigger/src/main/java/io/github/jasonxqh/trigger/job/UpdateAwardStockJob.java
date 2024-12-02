package io.github.jasonxqh.trigger.job;

import io.github.jasonxqh.domain.strategy.model.vo.StrategyAwardStockKeyVO;
import io.github.jasonxqh.domain.strategy.service.IRaffleStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/14, 星期四
 * @Description : 更新奖品库存任务，为了不让更新库存的压力打到数据库中，采用了redis更新缓存库存，异步队列更新数据库，数据库表最终
 * 一致即可
 **/

@Slf4j
@Component()
public class UpdateAwardStockJob {

    @Resource
    private IRaffleStock raffleStock;


    @Scheduled(cron = "0/5 * * * * ?")
    public void exec(){
        try {
//            log.info("定时任务，更新奖品消耗库存,降低对数据库的更新频次， 不要产生竞争");
            List<StrategyAwardStockKeyVO> strategyAwardStockKeyVOS = raffleStock.takeQueueValues();
            for(StrategyAwardStockKeyVO strategyAwardStockKeyVO : strategyAwardStockKeyVOS){
                raffleStock.updateStrategyAwardStock(
                            strategyAwardStockKeyVO.getStrategyId(),
                            strategyAwardStockKeyVO.getAwardId()
                    );
            }
        }catch (Exception e){
            log.error("定时任务，更新奖品消耗库存失败",e);
        }
    }
}
