package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SysDict;

/**
 * 数据字典表，存储字典类型定义(SysDict)表数据库访问层
 *
 * @author zhn
 */
@Mapper
public interface SysDictDao extends BaseMapper<SysDict> {

}
