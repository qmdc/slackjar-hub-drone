package com.slack.slackjarservice.drone.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slack.slackjarservice.drone.entity.DetectionHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetectionHistoryDao extends BaseMapper<DetectionHistory> {
}
