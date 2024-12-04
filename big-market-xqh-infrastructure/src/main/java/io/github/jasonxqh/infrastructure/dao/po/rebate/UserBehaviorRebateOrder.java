package io.github.jasonxqh.infrastructure.dao.po.rebate;

import java.util.Date;
import lombok.Data;

/**
 * 用户行为返利流水订单表
 */
@Data
public class UserBehaviorRebateOrder {
    /**
    * 自增ID
    */
    private Integer id;

    /**
    * 用户ID
    */
    private String userId;

    /**
    * 订单ID
    */
    private String orderId;

    /**
    * 行为类型（sign 签到、openai_pay 支付）
    */
    private String behaviorType;

    /**
    * 返利描述
    */
    private String rebateDesc;

    /**
    * 返利类型（sku 活动库存充值商品、integral 用户活动积分）
    */
    private String rebateType;

    /**
    * 返利配置【sku值，积分值】
    */
    private String rebateConfig;

    /**
    * 业务ID - 拼接的唯一值
    */
    private String bizId;
    /** 业务仿重ID - 外部透传，方便查询使用 */
    private String outBusinessNo;
    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;
}