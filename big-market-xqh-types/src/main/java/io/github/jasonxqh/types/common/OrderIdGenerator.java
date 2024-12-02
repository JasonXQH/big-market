package io.github.jasonxqh.types.common;


import java.util.Random;
public class OrderIdGenerator {

    private static final Random random = new Random();
    // 随机部分长度，可以根据需要配置
    private static final int RANDOM_LENGTH = 4;
    // 订单 ID 总长度，包含时间戳和随机部分
    private static final int ORDER_ID_LENGTH = 12;

    public String generateOrderId() {
        // 获取当前时间戳（毫秒）
        long timestamp = System.currentTimeMillis();
        // 随机数，长度为 4 的数字
        int randomPart = random.nextInt((int) Math.pow(10, RANDOM_LENGTH));
        // 时间戳部分
        String timestampPart = Long.toString(timestamp);
        // 格式化随机数，确保长度一致
        String randomPartString = String.format("%0" + RANDOM_LENGTH + "d", randomPart);
        // 拼接时间戳和随机数
        String orderId = timestampPart + randomPartString;
        // 如果生成的 ID 长度超过预设长度，则截断
        if (orderId.length() > ORDER_ID_LENGTH) {
            orderId = orderId.substring(0, ORDER_ID_LENGTH);
        }
        return orderId;
    }
}
