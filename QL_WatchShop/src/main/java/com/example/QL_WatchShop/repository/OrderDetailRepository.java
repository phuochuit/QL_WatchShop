package com.example.QL_WatchShop.repository;

import com.example.QL_WatchShop.model.Order;
import com.example.QL_WatchShop.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrderId(Integer orderId);
    List<OrderDetail> findByOrder(Order order);
}