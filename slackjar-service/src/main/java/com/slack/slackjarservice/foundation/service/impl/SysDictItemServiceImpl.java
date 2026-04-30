package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.foundation.dao.SysDictItemDao;
import com.slack.slackjarservice.foundation.entity.SysDictItem;
import com.slack.slackjarservice.foundation.service.SysDictItemService;
import org.springframework.stereotype.Service;

/**
 * 数据字典项表，存储字典项定义(SysDictItem)表服务实现类
 *
 * @author zhn
 */
@Service("sysDictItemService")
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemDao, SysDictItem> implements SysDictItemService {

}
