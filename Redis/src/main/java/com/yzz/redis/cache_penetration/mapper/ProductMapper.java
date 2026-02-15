package com.yzz.redis.cache_penetration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzz.redis.cache_penetration.domain.po.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
