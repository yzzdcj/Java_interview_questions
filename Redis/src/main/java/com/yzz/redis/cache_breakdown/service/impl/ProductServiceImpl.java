package com.yzz.redis.cache_breakdown.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzz.redis.cache_breakdown.domain.po.Product;
import com.yzz.redis.cache_breakdown.domain.po.RedisData;
import com.yzz.redis.cache_breakdown.mapper.ProductMapper2;
import com.yzz.redis.cache_breakdown.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("productServiceImpl2")
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper2, Product> implements ProductService {
    private final StringRedisTemplate stringRedisTemplate;
    private  final ThreadPoolTaskExecutor executor;
    @Override
    public Product queryWithMutex(Long id) {
        String s = stringRedisTemplate.opsForValue().get("product:" + id);
        if(s!=null){
            return JSONUtil.toBean(s,Product.class);
        }
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock:sku:" + id, "1",10,TimeUnit.SECONDS);
        try{
            if(Boolean.FALSE.equals(lock)){
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            s=stringRedisTemplate.opsForValue().get("product:"+id);
            if(s!=null){
                return JSONUtil.toBean(s,Product.class);
            }
            Product product = lambdaQuery().eq(Product::getId, id).one();
            stringRedisTemplate.opsForValue().set("product:"+id,JSONUtil.toJsonStr(product),30, TimeUnit.MINUTES);
            return product;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if(Boolean.TRUE.equals(lock))
            stringRedisTemplate.delete("lock:sku:"+id);
        }
    }

    @Override
    public Product queryWithLogicalExpire(Long id) {
        String p = stringRedisTemplate.opsForValue().get("product:" + id);
        if(p==null)
            return new Product();
        RedisData redisData = JSONUtil.toBean(p, RedisData.class);
        Product product = JSONUtil.toBean((JSONObject) redisData.getData(), Product.class);
        Boolean ifAfter = redisData.getExpireTime().isAfter(LocalDateTime.now());
        if(Boolean.TRUE.equals(ifAfter))
            return product;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock:sku:" + id,"1",10,TimeUnit.SECONDS);
        if(Boolean.FALSE.equals( lock))
            return product;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    Product product= lambdaQuery().eq(Product::getId, id).one();
                    RedisData<Product> redisData = new RedisData<>();
                    redisData.setData(product);
                    redisData.setExpireTime(LocalDateTime.now().plusMinutes(30));
                    stringRedisTemplate.opsForValue().set("product:"+id,JSONUtil.toJsonStr(redisData));
                }
                finally {
                    stringRedisTemplate.delete("lock:sku:"+id);
                }
            }
        });
        return product;
    }
}
