package com.yzz.redis.cache_avalanche.Service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.yzz.redis.cache_avalanche.Service.HomeRecommendService;
import com.yzz.redis.cache_avalanche.domain.po.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeRecommendServiceImpl implements HomeRecommendService {
    private final StringRedisTemplate stringRedisTemplate;
    private static  final String REDIS_KEY = "home:recommend:";
    @Override
    public void refreshRecommendProducts(List<Product> products) {
        //可用Pipeline加快
        for(Product product:products){
            stringRedisTemplate.opsForValue().set(REDIS_KEY+product.getId(), JSONUtil.toJsonStr( product),3600+RandomUtil.randomInt(0,300), TimeUnit.SECONDS);
        }
    }
}
