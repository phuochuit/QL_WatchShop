package com.example.QL_WatchShop.repository;

import com.example.QL_WatchShop.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Coupon findByCodeAndIsActiveTrue(String code);
}