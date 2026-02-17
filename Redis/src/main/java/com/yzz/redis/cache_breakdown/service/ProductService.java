package com.yzz.redis.cache_breakdown.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzz.redis.cache_breakdown.domain.po.Product;

public interface ProductService extends IService<Product> {
    public  Product queryWithMutex(Long id);
    public  Product queryWithLogicalExpire(Long id);
}
