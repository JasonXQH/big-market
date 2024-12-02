package io.github.jasonxqh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/17, 星期日
 * @Description :
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardListResponseDTO {
    // 奖品ID
    private Integer awardId;
    // 奖品标题
    private String awardTitle;
    // 奖品副标题【抽奖1次后解锁】
    private String awardSubtitle;
    // 排序编号
    private Integer sort;
    // 其他信息：是否解锁,true代表已解锁，false代表未解锁
    private Boolean isAwardUnlock;
    // 其他信息: 抽奖几次才能解锁,未配置则为空
    private Integer awardRuleLockCount;
    // 其他信息：再抽几次就可以解锁
    private Integer waitUnlockCount;
}
