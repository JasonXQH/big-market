package io.github.jasonxqh.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽奖活动账户表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityAccountEntity {

    /**
    * 用户ID
    */
    private String userId;

    /**
    * 活动ID
    */
    private Long activityId;

    /**
    * 总次数
    */
    private Integer totalCount;

    /**
    * 总次数-剩余
    */
    private Integer totalCountSurplus;

    /**
    * 日次数
    */
    private Integer dayCount;

    /**
    * 日次数-剩余
    */
    private Integer dayCountSurplus;

    /**
    * 月次数
    */
    private Integer monthCount;

    /**
    * 月次数-剩余
    */
    private Integer monthCountSurplus;
}