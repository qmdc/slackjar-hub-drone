package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SysFile;

/**
 * 系统文件表(SysFile)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:27:42
 */
@Mapper
public interface SysFileDao extends BaseMapper<SysFile> {

}

