package io.github.jasonxqh.domain.rebate.model.entity;

import io.github.jasonxqh.domain.rebate.model.event.SendBehaviorRebateEvent;
import io.github.jasonxqh.domain.rebate.model.vo.TaskStateVO;
import io.github.jasonxqh.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    /** 用户ID */
    private String userId;
    /**
     * 消息编号
     */
    private String messageId;

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息主体
     */

    private BaseEvent.EventMessage<SendBehaviorRebateEvent.SendBehaviorRebateMessage> message;

    /**
     * 任务状态；create-创建、completed-完成、fail-失败
     */
    private TaskStateVO state;
}
