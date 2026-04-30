package com.slack.slackjarservice.common.process;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * MyBatis配置
 * @author zhn
 */
@Slf4j
@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill.....");
        long currentTimeMillis = System.currentTimeMillis();
        this.setFieldValByName("createTime", currentTimeMillis, metaObject);
        this.setFieldValByName("updateTime", currentTimeMillis, metaObject);
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
         log.info("start update fill.....");
        this.setFieldValByName("updateTime", System.currentTimeMillis(), metaObject);
    }

}
