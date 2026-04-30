package com.slack.slackjarservice.common.config;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Long类型随机不重复ID生成工具类
 * 使用雪花算法原理，确保生成的ID为Long类型且不重复
 * ID结构：1位符号位(始终为0) + 41位时间戳 + 10位机器ID + 12位序列号
 */
@Slf4j
public class LongIdGenerator {

    // 起始时间戳 (2024-01-01)
    private static final long EPOCH = LocalDateTime.of(2024, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();

    // 时间戳位数
    private static final int TIMESTAMP_BITS = 41;
    // 机器ID位数
    private static final int MACHINE_ID_BITS = 10;
    // 序列号位数
    private static final int SEQUENCE_BITS = 12;

    // 机器ID最大值 (1023)
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    // 序列号最大值 (4095)
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 时间戳移位
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    // 机器ID移位
    private static final int MACHINE_ID_SHIFT = SEQUENCE_BITS;

    // 机器ID
    private final long machineId;
    // 序列号
    private final AtomicLong sequence = new AtomicLong(0);
    // 上一次时间戳
    private volatile long lastTimestamp = -1L;

    // 单例实例
    private static volatile LongIdGenerator instance;

    private LongIdGenerator(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("机器ID超出范围，应在 [0, " + MAX_MACHINE_ID + "] 之间");
        }
        this.machineId = machineId;
    }

    /**
     * 获取单例实例
     * 默认机器ID为0
     *
     * @return LongIdGenerator实例
     */
    public static LongIdGenerator getInstance() {
        return getInstance(0L);
    }

    /**
     * 获取单例实例
     *
     * @param machineId 机器ID，范围 [0, 1023]
     * @return LongIdGenerator实例
     */
    public static LongIdGenerator getInstance(long machineId) {
        if (instance == null) {
            synchronized (LongIdGenerator.class) {
                if (instance == null) {
                    instance = new LongIdGenerator(machineId);
                }
            }
        }
        return instance;
    }

    /**
     * 生成下一个Long类型的唯一ID
     *
     * @return Long类型的唯一ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            log.error("时钟回退，拒绝生成ID直到时间追上");
            throw new RuntimeException("时钟回退，拒绝生成ID直到时间追上");
        }

        // 如果是同一毫秒内生成的，则进行序列号递增
        if (lastTimestamp == timestamp) {
            long sequenceValue = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (sequenceValue == 0) {
                // 序列号达到最大值，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号从0开始
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        // 组合ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
               (machineId << MACHINE_ID_SHIFT) |
               sequence.get();
    }

    /**
     * 阻塞到下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 解析ID中的时间戳部分
     *
     * @param id 生成的ID
     * @return 对应的时间戳
     */
    public static long extractTimestamp(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        return timestamp;
    }

    /**
     * 解析ID中的机器ID部分
     *
     * @param id 生成的ID
     * @return 机器ID
     */
    public static long extractMachineId(long id) {
        return (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;
    }

    /**
     * 解析ID中的序列号部分
     *
     * @param id 生成的ID
     * @return 序列号
     */
    public static long extractSequence(long id) {
        return id & MAX_SEQUENCE;
    }
}
