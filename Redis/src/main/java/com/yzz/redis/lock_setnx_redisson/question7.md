####【考核题目】：
    分别利用 Redis原生命令 (SETNX) 和 Redisson 框架 实现分布式锁，解决 “优惠券抢购” 的超卖问题。

####【业务背景】：
    营销中心搞活动，发放“满100减50”的黄金券，总量只有 10 张。

    活动开始瞬间，1 万个用户同时请求抢券接口。

    我们需要保证：

        互斥性：同一时刻只能有一个线程去扣减库存。

        死锁避免：如果抢到锁的服务宕机了，锁必须能自动释放。

        安全性：自己加的锁，只能自己删（防止张三释放了李四的锁）。

####【具体需求】：

    编写 CouponService 类，实现两个方法：

####⚔️ 挑战一：手写 SETNX 锁 (Hard Mode)
    方法：void grabCouponNative(Long couponId)

    工具：StringRedisTemplate

    核心逻辑：

    加锁：使用 opsForValue().setIfAbsent(key, value, timeout, unit)。

        考点 1：Value 应该存什么？（为了防止误删，必须存一个唯一标识，如 UUID）。

        业务：模拟抢券耗时（Sleep）。

    解锁：

        考点 2：在 finally 块中释放锁。

        考点 3：释放前必须判断锁是不是自己的（对比 Value）。

    注意：这里为了简化，可以用 Java 代码判断 + 删除（虽然非原子性，但考察的是思路）。严格来说应该用 Lua。

####🛡️ 挑战二：Redisson 锁 (Enterprise Mode)
    方法：void grabCouponRedisson(Long couponId)

    工具：RedissonClient

    核心逻辑：

        获取锁对象 RLock。
    
        尝试加锁（阻塞等待）。
    
        执行业务。

        释放锁。

    核心提问：在使用 Redisson 时，如果你不设置锁的过期时间（不传 leaseTime），Redisson 是如何保证锁不过期，且服务宕机后又能自动释放的？（请口述或注释写出 “看门狗 Watchdog” 机制）。

####【数据模型】：

    Lock Key: lock:coupon:{id}

####请提供：

    grabCouponNative 的代码实现。
    
    grabCouponRedisson 的代码实现。
    
    简短解释 Redisson 的“看门狗”是干嘛的。__