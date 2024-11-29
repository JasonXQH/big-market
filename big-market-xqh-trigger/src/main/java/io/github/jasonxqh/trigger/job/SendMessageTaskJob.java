package io.github.jasonxqh.trigger.job;


import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import io.github.jasonxqh.domain.task.model.entity.TaskEntity;
import io.github.jasonxqh.domain.task.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.nio.ch.ThreadPool;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component()
public class SendMessageTaskJob {
    @Resource
    private ITaskService taskService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private IDBRouterStrategy dbRouter;

    //扫描task，分库不分表
    @Scheduled(cron = "0/5 * * * * ?")
    public void exec(){
        try {
            // 获取分库数量
            int dbCount = dbRouter.dbCount();
            for(int dbIdx = 1 ; dbIdx <= dbCount ; dbIdx++){
                int findDbIdx = dbIdx;
                threadPoolExecutor.execute(() -> {
                    try {
                        dbRouter.setDBKey(findDbIdx);
                        dbRouter.setTBKey(0);
                        List<TaskEntity> taskEntities = taskService.queryNoSendMessageTaskList();
                        for(TaskEntity taskEntity : taskEntities){
                            threadPoolExecutor.execute(() -> {
                                try {
                                    taskService.sendMessage(taskEntity);
                                    taskService.updateTaskSendMessageCompleted(taskEntity.getUserId(),taskEntity.getMessageId());
                                }catch (Exception e){
                                    log.error("定时任务，发送MQ消息失败 userId:{} topic:{}", taskEntity.getUserId(), taskEntity.getTopic());
                                    taskService.updateTaskSendMessageFail(taskEntity.getUserId(),taskEntity.getMessageId());
                                }
                            });
                        }

                    } finally {
                        dbRouter.clear();
                    }
                });
            }

        } catch (Exception e) {
            log.error("定时任务，扫描MQ任务表，发送消息失败。",e);
            throw new RuntimeException(e);
        } finally {
            dbRouter.clear();
        }
    }
}
