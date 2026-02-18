package com.yzz.redis.cache_consistency.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzz.redis.cache_consistency.domain.po.Product;
import com.yzz.redis.cache_consistency.mapper.ProductConsistencyMapper;
import com.yzz.redis.cache_consistency.service.ProductConsistencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductConsistencyServiceImpl extends ServiceImpl<ProductConsistencyMapper, Product> implements ProductConsistencyService{
    private final StringRedisTemplate stringRedisTemplate;
    private final Executor threadPoolTaskExecutor;
    private final RedissonClient redissonClient;
    @Override
    public void updateWithDoubleDelete(Long id, Product p) {
        stringRedisTemplate.delete("product:"+id);
        update(p,new UpdateWrapper<Product>().eq("id",id));
        threadPoolTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    stringRedisTemplate.delete("product:"+id);
                } catch (InterruptedException e) {
                    log.error("error",e);
                }
            }
        });
    }

    @Override
    public Product getWithRwLock(Long id) {
        String p = stringRedisTemplate.opsForValue().get("product:" + id);
        if(p!=null)
            return JSONUtil.toBean(p,Product.class);

        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("lock:product:" + id);
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try{
            Product product = getById(id);
            stringRedisTemplate.opsForValue().set("product:" + id, JSONUtil.toJsonStr(product),30, TimeUnit.MINUTES);
            return product;
        }
        finally {
            rLock.unlock();
        }
    }

    @Override
    public void updateWithRwLock(Long id, Product p) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("lock:product:" + id);
        RLock wLock = readWriteLock.writeLock();
        wLock.lock();
        try{
            update(p,new UpdateWrapper<Product>().eq("id",id));
            stringRedisTemplate.delete("product:"+id);
        }
        finally {
            wLock.unlock();
        }
    }
}
