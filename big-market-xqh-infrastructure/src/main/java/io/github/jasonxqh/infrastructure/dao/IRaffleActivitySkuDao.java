package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.activity.RaffleActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRaffleActivitySkuDao {
    RaffleActivitySku queryActivitySku(Long sku);

    void updateSkuStock(RaffleActivitySku raffleActivitySkuReq);

    void clearActivitySkuStock(RaffleActivitySku skuReq);

    List<RaffleActivitySku> queryActivitySkuByActivityId(Long activityId);
}