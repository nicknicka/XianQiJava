package com.xx.xianqijava.config;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 雪花算法ID生成器
 *
 * <p>基于Twitter Snowflake算法实现，生成64位长整型唯一ID
 *
 * <p>ID结构（64位）：
 * <pre>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * ↑   └─────── 41位时间戳 ────────┘ ↑                                      ↑    ↑    └─ 12位序列号
 * │                              10位机器ID                            │    └───── 5位数据中心ID
 * 固定值（1位）                                                         └─────────── WorkerID
 * </pre>
 *
 * <p>特点：
 * <ul>
 *   <li>全局唯一</li>
 *   <li>趋势递增</li>
 *   <li>高性能</li>
 *   <li>无需依赖数据库</li>
 * </ul>
 *
 * @author Claude
 * @since 2025-03-07
 */
@Component
public class SnowflakeIdGenerator {

    // 起始时间戳 (2024-01-01 00:00:00)
    private static final long EPOCH = 1704067200000L;

    // 各部分位数
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // 各部分最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 各部分位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // 机器ID和数据中心ID
    private final long workerId;
    private final long datacenterId;

    // 序列号
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 时钟回拨容忍度（毫秒）
    private static final long MAX_CLOCK_OFFSET = 5000L;

    /**
     * 构造函数
     *
     * <p>使用默认配置：workerId=1, datacenterId=1
     */
    public SnowflakeIdGenerator() {
        this(1L, 1L);
    }

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0-31)
     * @param datacenterId 数据中心ID (0-31)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个ID
     *
     * @return 雪花算法生成的ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 时钟回拨处理
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= MAX_CLOCK_OFFSET) {
                // 等待时钟追上
                timestamp = tilNextMillis(lastTimestamp);
            } else {
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
            }
        }

        // 同一毫秒内，序列号自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为随机值（避免连续ID可预测）
            sequence = new SecureRandom().nextInt((int) MAX_SEQUENCE);
        }

        lastTimestamp = timestamp;

        // 生成ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成批量ID
     *
     * @param count 生成数量
     * @return ID数组
     */
    public long[] nextIds(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }

        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * 解析ID，获取生成时间戳
     *
     * @param id 雪花ID
     * @return 生成时间戳（毫秒）
     */
    public long parseTimestamp(long id) {
        return ((id >> TIMESTAMP_SHIFT) & ~(-1L << 41)) + EPOCH;
    }

    /**
     * 解析ID，获取数据中心ID
     *
     * @param id 雪花ID
     * @return 数据中心ID
     */
    public long parseDatacenterId(long id) {
        return (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 解析ID，获取工作ID
     *
     * @param id 雪花ID
     * @return 工作ID
     */
    public long parseWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 解析ID，获取序列号
     *
     * @param id 雪花ID
     * @return 序列号
     */
    public long parseSequence(long id) {
        return id & MAX_SEQUENCE;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳（毫秒）
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取WorkerID
     *
     * @return WorkerID
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 获取DatacenterID
     *
     * @return DatacenterID
     */
    public long getDatacenterId() {
        return datacenterId;
    }

    /**
     * 获取上次生成ID的时间戳
     *
     * @return 时间戳（毫秒）
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }
}
