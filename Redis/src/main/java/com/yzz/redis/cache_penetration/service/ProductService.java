package com.yzz.redis.cache_penetration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzz.redis.cache_penetration.domain.po.Product;

public interface ProductService extends IService<Product> {
    Product getProductById(Long id);
}
