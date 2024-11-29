package io.github.jasonxqh.infrastructure.adapter.repository;

import io.github.jasonxqh.domain.task.model.entity.TaskEntity;
import io.github.jasonxqh.domain.task.repository.ITaskRepository;
import io.github.jasonxqh.infrastructure.dao.ITaskDao;
import io.github.jasonxqh.infrastructure.dao.po.Task;
import io.github.jasonxqh.infrastructure.event.EventPublisher;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TaskRepository implements ITaskRepository {
    @Resource
    private ITaskDao taskDao;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        List<Task> tasks = taskDao.queryNoSendMessageTaskList();
        return tasks.stream().map(task -> {
            TaskEntity entity = new TaskEntity();
            entity.setTopic(task.getTopic());
            entity.setUserId(task.getUserId());
            entity.setMessageId(task.getMessageId());
            entity.setMessage(task.getMessage());
            return entity;
        }).collect(Collectors.toList());

    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        Task taskReq = Task.builder()
                .messageId(messageId)
                .userId(userId)
                .build();
        taskDao.updateTaskSendMessageCompleted(taskReq);

    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        Task taskReq = Task.builder()
                .messageId(messageId)
                .userId(userId)
                .build();
        taskDao.updateTaskSendMessageFail(taskReq);


    }
}
