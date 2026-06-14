package com.example.QL_WatchShop.service;

import com.example.QL_WatchShop.model.Coupon;
import com.example.QL_WatchShop.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeAndIsActiveTrue(code);    }
}