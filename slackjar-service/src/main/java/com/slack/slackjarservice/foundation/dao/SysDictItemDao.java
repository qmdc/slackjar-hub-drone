package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SysDictItem;

/**
 * 数据字典项表，存储字典项定义(SysDictItem)表数据库访问层
 *
 * @author zhn
 */
@Mapper
public interface SysDictItemDao extends BaseMapper<SysDictItem> {

}
