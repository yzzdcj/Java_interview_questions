package com.yzz.redis.cache_consistency.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductUpdateListener {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 核心考点：
     * 1. 增加 Channel 参数：用于手动操作 ACK。
     * 2. 增加 @Header(AmqpHeaders.DELIVERY_TAG) long tag：获取消息的唯一 ID。
     */
    @RabbitListener(queues = "product.delete.queue")
    public void delete(String productId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.info("收到删除缓存消息，商品ID: {}", productId);

            // 1. 执行业务逻辑
            stringRedisTemplate.delete("product:" + productId);

            // 2. 成功：手动签收 (ACK)
            // basicAck(deliveryTag, multiple)
            // multiple=false 表示只签收当前这一条，不批量签收
            channel.basicAck(tag, false);
            log.info("消息处理成功，已 ACK");

        } catch (Exception e) {
            log.error("删除缓存失败，商品ID: {}, 异常: {}", productId, e.getMessage());

            try {
                // 3. 失败：手动拒收 (NACK)
                // basicNack(deliveryTag, multiple, requeue)
                // requeue=true:  重回队列，立即重试 (如果 Redis 一直挂，会死循环！慎用)
                // requeue=false: 丢弃消息，或者进入死信队列 (DLQ)

                // 企业级做法：通常设置为 false，让它进死信队列，由人工去排查，防止阻塞主队列
                channel.basicNack(tag, false, false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}