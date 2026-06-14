package com.example.QL_WatchShop.repository;

import com.example.QL_WatchShop.model.Product;
import com.example.QL_WatchShop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductId(Integer productId);
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
}