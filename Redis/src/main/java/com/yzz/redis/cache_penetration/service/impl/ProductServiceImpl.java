package com.yzz.redis.cache_penetration.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzz.redis.cache_penetration.domain.po.Product;
import com.yzz.redis.cache_penetration.mapper.ProductMapper;
import com.yzz.redis.cache_penetration.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper,Product> implements ProductService  {
    private final StringRedisTemplate stringRedisTemplate;
    private final RBloomFilter< String> bloomFilter;
    @Override
    public Product getProductById(Long id) {
        //方案一
        String p = stringRedisTemplate.opsForValue().get("product:detail:" + id);
        if(StrUtil.isNotBlank(p)){
            return JSONUtil.toBean(p,Product.class);
        }
        if("".equals( p)){
            return new Product();
        }
        Product product = lambdaQuery().eq(Product::getId, id).one();
        if(product!=null){
            stringRedisTemplate.opsForValue().set("product:detail:" + id, JSONUtil.toJsonStr(product),30,TimeUnit.MINUTES);
            return product;
        }
        stringRedisTemplate.opsForValue().set("product:detail:" + id, "", 5, TimeUnit.MINUTES);
        return new Product();
        /*方案二,假设数据已全放入布隆过滤器中
        boolean isContains = bloomFilter.contains(String.valueOf(id));
        if(Boolean.FALSE.equals(isContains)){
            return null;
        }
        String p = stringRedisTemplate.opsForValue().get("product:detail:" + id);
        if(p!=null){
            return JSONUtil.toBean(p,Product.class);
        }
        Product product = lambdaQuery().eq(Product::getId, id).one();
        if(product!=null){
        stringRedisTemplate.opsForValue().set("product:detail:" + id, JSONUtil.toJsonStr(product), 30, TimeUnit.MINUTES);
        else
        stringRedisTemplate.opsForValue().set("product:detail:" + id, "", 5, TimeUnit.MINUTES);
        return product;*/
    }
}
