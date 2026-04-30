package com.slack.slackjarservice.foundation.filepolicy;

import com.slack.slackjarservice.common.constant.ConfigKeys;
import com.slack.slackjarservice.common.enumtype.foundation.ConfigEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.enumtype.foundation.StorageVendorEnum;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文件存储策略管理器
 * 负责根据配置动态管理文件存储策略的初始化和切换
 */
@Slf4j
@Component
public class FileStoragePolicyManager {

    @Value("${system.default.oss.active}")
    protected String activeVendor;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private SysConfigService sysConfigService;

    private volatile FileStoragePolicy activePolicy;

    private volatile StorageVendorEnum currentVendor;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<StorageVendorEnum, FileStoragePolicy> policyMap = new EnumMap<>(StorageVendorEnum.class);

    /**
     * 初始化所有文件存储策略
     */
    @PostConstruct
    private void init() {
        log.info("初始化文件存储策略-start");
        // 获取所有文件存储策略实现
        Map<String, FileStoragePolicy> policies = applicationContext.getBeansOfType(FileStoragePolicy.class);

        policies.forEach((beanName, policy) -> {
            StorageVendorEnum vendorType = policy.getVendorType();
            policyMap.put(vendorType, policy);
            log.info("注册文件存储策略: {}", vendorType.getDescription());
        });
        log.info("初始化文件存储策略-end，共加载 {} 个策略", policyMap.size());

        // 根据配置初始化当前生效的策略
        refreshManager();
    }

    /**
     * 刷新存储策略管理器
     */
    public Boolean refreshManager() {
        if (refreshActivePolicy()) {
            log.info("刷新OSS存储策略成功");
            return true;
        } else {
            log.info("刷新OSS存储策略失败,触发重试");
            if (refreshActivePolicy()) {
                log.info("重试成功");
                return true;
            } else {
                log.error("重试失败");
                return false;
            }
        }
    }

    /**
     * 线程安全地刷新当前生效的策略
     */
    private Boolean refreshActivePolicy() {
        lock.writeLock().lock();
        try {
            // 从系统配置中获取当前生效的存储厂商
            StorageVendorEnum vendorEnum = StorageVendorEnum.valueOf(getActiveStorageVendor().toUpperCase());

            if (activePolicy != null) {
                try {
                    activePolicy.destroy();
                    log.info("销毁之前的存储策略: {}", currentVendor.getDescription());
                } catch (Exception e) {
                    log.error("销毁存储策略时发生异常", e);
                }
            }

            // 初始化新的策略
            FileStoragePolicy newPolicy = policyMap.get(vendorEnum);
            if (newPolicy != null) {
                try {
                    log.info("切换到新的存储策略:{}", vendorEnum.getDescription());
                    newPolicy.init();
                    activePolicy = newPolicy;
                    currentVendor = vendorEnum;
                    return true;
                } catch (Exception e) {
                    log.error("初始化存储策略失败:", e);
                    return false;
                }
            } else {
                log.error("未找到对应的存储策略: {}", vendorEnum);
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取当前配置生效的存储厂商
     */
    protected String getActiveStorageVendor() {
        try {
            SysConfigResponse configResponse = sysConfigService.getConfigByCategory(ConfigEnum.SYSTEM_PARAMS.getKey());
            if (configResponse != null && configResponse.getConfigItems() != null) {
                return configResponse.getConfigItems().stream()
                        .filter(item -> ConfigKeys.SystemParams.ACTIVE_FILE_STORAGE.equals(item.getConfigKey()))
                        .findFirst()
                        .map(SysConfigResponse.ConfigItem::getConfigValue)
                        .orElse(activeVendor);
            }
        } catch (Exception e) {
            log.warn("获取系统配置失败，使用配置的默认存储厂商: {}", activeVendor, e);
        }
        return activeVendor;
    }

    /**
     * 线程安全地获取当前生效的策略
     * 使用读锁允许多个线程并发读取
     */
    public FileStoragePolicy getPolicy() {
        lock.readLock().lock();
        try {
            AssertUtil.notNull(activePolicy, ResponseEnum.FILE_STORAGE_STRATEGY);
            return activePolicy;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取当前系统初始化的存储厂商
     */
    public StorageVendorEnum getCurrentVendor() {
        lock.readLock().lock();
        try {
            return currentVendor;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 销毁所有策略
     */
    @PreDestroy
    public void destroy() {
        lock.writeLock().lock();
        try {
            policyMap.values().forEach(FileStoragePolicy::destroy);
            activePolicy = null;
            currentVendor = null;
            log.info("所有文件存储策略已销毁");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
