package io.github.jasonxqh.infrastructure.dao;

import io.github.jasonxqh.infrastructure.dao.po.award.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author : jasonxu
 * @mailto : xuqihang74@gmail.com
 * @created : 2024/11/7, 星期四
 * @Description : 奖品表dao
 **/

@Mapper
public interface IAwardDao {
    List<Award> queryAwardList();

    String queryAwardConfigByAwardId(Integer awardId);

    String queryAwardKeyByAwardId(Integer awardId);
}
