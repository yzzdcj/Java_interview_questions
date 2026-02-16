package com.yzz.redis.cache_avalanche.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzz.redis.cache_avalanche.domain.po.Product;

public interface ProductService extends IService<Product> {
    public  Product queryWithMutex(Long id);
    public  Product queryWithLogicalExpire(Long id);
}
