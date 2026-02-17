package com.yzz.redis.cache_avalanche.Service;

import com.yzz.redis.cache_avalanche.domain.po.Product;

import java.util.List;

public interface HomeRecommendService {
    void refreshRecommendProducts(List<Product> products);
}
