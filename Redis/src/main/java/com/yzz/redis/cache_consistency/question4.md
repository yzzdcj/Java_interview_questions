####【考核题目】：
    实现 Redis 与 MySQL 的 双写一致性 三种方案。

####【业务背景】：
    我们负责“商品中心”的核心服务。

    写场景：运营在后台修改商品价格（低频）。

    读场景：C端用户查商品详情（高频）。

####【具体需求】：

    请编写 ProductConsistencyService 类，实现以下三个方法/模块：

####⚔️ 方案一：延时双删 (Delayed Double Delete)
    方法：updateWithDoubleDelete(Long id, Product p)

    逻辑：

        先删 Redis。

        更新数据库。

        休眠 500ms（模拟业务耗时）。

        再次删 Redis。

    考点：这里休眠的代码不能卡主线程，必须用异步线程池来执行最后那次删除。

####🛡️ 方案二：MQ 异步重试 (最终一致性)
    组件：ProductUpdateListener (消费者)。

    逻辑：

        监听队列 product.delete.queue。
        
        收到消息（包含 productId）后，执行 redis.delete。
    
    核心考点：如果 delete 抛异常了（比如 Redis 抖动），如何通过 RabbitMQ 的 手动 ACK 机制让消息回到队列重试？（不要死循环，简单演示 basicNack 即可）。

####🔒 方案三：读写锁 (Redisson RReadWriteLock) —— 强一致性
    这是本次新增的重头戏。当运营正在改价格时，所有读请求必须等待，直到价格改完。

    方法 A (读)：Product getWithRwLock(Long id)

        逻辑：先加 读锁 (ReadLock)，再查 Redis/DB，写入 Redis，最后释放锁。

        特点：读锁和读锁是共享的，不互斥（高并发）。

    方法 B (写)：void updateWithRwLock(Long id, Product p)

        逻辑：先加 写锁 (WriteLock)，再更新 DB，删除 Redis（或者更新 Redis），最后释放锁。

        特点：写锁和读锁是互斥的。

####【数据模型】：

    Redis Key: product:1001

    Lock Key: lock:product:1001

####请提供：

    方案一（延时双删）的代码。

    方案二（MQ 监听器）的代码。

    方案三（读写锁）的代码（需注入 RedissonClient）。