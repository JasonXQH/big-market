package io.github.jasonxqh.types.common;

public class SnowFlake {

    private long datacenterId;
    private long workerId;
    private long sequence;

    public SnowFlake(long workerId, long datacenterId) {
        this(workerId, datacenterId, 0);
    }

    public SnowFlake(long workerId, long datacenterId, long sequence) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        System.out.printf("worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
                timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId);

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }

    private long twepoch = 1634393012000L;  // 开始时间戳（2021-10-16 22:03:32）
    private long datacenterIdBits = 5L;     // 机房号，的ID所占的位数 5个bit
    private long workerIdBits = 5L;         // 机器ID所占的位数 5个bit
    private long maxWorkerId = -1L ^ (-1L << workerIdBits); // 机器ID最大值
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 机房ID最大值
    private long sequenceBits = 12L;        // 同一时间的序列所占的位数 12个bit
    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits); // 序列号掩码
    private long lastTimestamp = -1L;

    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public synchronized String nextId() {
        long timestamp = timeGen();  // 获取当前时间戳，单位毫秒

        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;  // 序列号自增

            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);  // 如果序列号溢出，则等待下一个毫秒
            }
        } else {
            sequence = 0;  // 当前时间戳的第一次调用，序列号重置为0
        }

        lastTimestamp = timestamp;

        // 生成 Snowflake ID
        long id = ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;

        // 将长整型ID转换为字符串
        String idString = Long.toString(id);

        // 从尾部截取12位
        if (idString.length() > 12) {
            return idString.substring(idString.length() - 12);
        } else {
            // 如果 ID 长度小于12位，直接返回
            return idString;
        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake worker = new SnowFlake(1, 1);
        for (int i = 0; i < 10; i++) {
            System.out.println(worker.nextId());  // 输出截取后的订单 ID
        }
    }
}
