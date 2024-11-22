package io.github.jasonxqh.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 抽奖活动次数配置表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleActivityCountEntity {

    /**
    * 活动次数编号
    */
    private Long activityCountId;

    /**
    * 总次数
    */
    private Integer totalCount;

    /**
    * 日次数
    */
    private Integer dayCount;

    /**
    * 月次数
    */
    private Integer monthCount;
}