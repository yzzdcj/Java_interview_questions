package com.yzz.redis.lock_setnx_redisson.service.impl;

import cn.hutool.core.lang.UUID;
import com.yzz.redis.lock_setnx_redisson.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String COUPON_LOCK = "coupon:lock:";
    private final RedissonClient redissonClient;
    @Override
    public void grabCouponNative(Long couponId) {
        // 锁 Key
        String key = COUPON_LOCK + couponId;
        // 唯一标识 (UUID)，用于防止误删
        String uuid = UUID.fastUUID().toString(true);

        // 1. 循环重试 (替代递归)
        while (true) {
            // 2. 加锁 (SETNX)
            Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(key, uuid, 30, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(isLock)) {
                // 抢到锁
                try {
                    // 3. 执行业务
                    log.info("抢到原生锁，开始扣减库存...");
                    Thread.sleep(50);
                    break; // 业务执行完，跳出循环
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    // 4. 释放锁 (原子性 Lua 脚本是最佳实践，这里用 Java 逻辑兜底)
                    String value = stringRedisTemplate.opsForValue().get(key);
                    // 只有 Value 是自己的 UUID 才删除
                    if (uuid.equals(value)) {
                        stringRedisTemplate.delete(key);
                        log.info("释放原生锁");
                    }
                }
            } else {
                // 5. 没抢到锁，休眠后重试 (防止 CPU 空转)
                try {
                    Thread.sleep(50); // 稍微睡一下
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("未抢到锁，正在重试...");
            }
        }
    }

    @Override
    public void grabCouponRedisson(Long couponId) {
        String key = COUPON_LOCK + couponId;
        // 1. 获取锁对象 (此时还没加锁)
        RLock lock = redissonClient.getLock(key);

        boolean isLocked = false;
        try {
            // 2. 尝试加锁
            // 参数1 waitTime: 最多等 10ms，等不到就放弃 (快速失败)
            // 参数2 leaseTime: 传 -1 (或不传)，开启【看门狗】机制
            isLocked = lock.tryLock(10, TimeUnit.MILLISECONDS);

            if (isLocked) {
                // 3. 抢到锁，执行业务
                log.info("抢到 Redisson 锁，执行业务...");
                Thread.sleep(50);
            } else {
                // 没抢到锁的处理 (比如抛出异常，或记录日志)
                log.info("手慢了，优惠券被抢光了 (Redisson)");
            }
        } catch (InterruptedException e) {
            log.error("加锁被中断", e);
        } finally {
            // 4. 【关键】只有抢到了锁，且锁还被当前线程持有，才能释放
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放 Redisson 锁");
            }
        }
    }
}
