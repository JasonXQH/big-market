package io.github.jasonxqh.infrastructure.dao.po;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 任务表，发送MQ
 */
@Data
@Builder
public class Task {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息编号
     */
    private String messageId;

    /**
     * 消息主体
     */
    private String message;

    /**
     * 任务状态；create-创建、completed-完成、fail-失败
     */
    private String state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}