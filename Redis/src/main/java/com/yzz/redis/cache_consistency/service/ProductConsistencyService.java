package com.yzz.redis.cache_consistency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzz.redis.cache_consistency.domain.po.Product;

public interface ProductConsistencyService extends IService<Product> {
    void updateWithDoubleDelete(Long id, Product p);
    Product getWithRwLock(Long id);
    void updateWithRwLock(Long id, Product p);
}
