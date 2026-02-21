package com.yzz.redis.lock_setnx_redisson.service;

public interface CouponService {
    void grabCouponNative(Long couponId);
    void grabCouponRedisson(Long couponId);
}
