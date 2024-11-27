package io.github.jasonxqh.domain.activity.model.entity;

import lombok.*;

/**
 * @author jasonxu
 * @date 2024/11/26
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartakeRaffleActivityEntity {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;
}
