package com.yzz.redis.cache_consistency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzz.redis.cache_consistency.domain.po.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductConsistencyMapper extends BaseMapper<Product> {
}
