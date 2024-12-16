package io.github.jasonxqh.domain.activity.service;


import io.github.jasonxqh.domain.activity.model.entity.SkuProductEntity;

import java.util.List;

public interface IRaffleActivitySkuProductService {
    List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId);
}
