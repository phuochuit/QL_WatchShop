package com.example.QL_WatchShop.repository;

import com.example.QL_WatchShop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findTop4ByOrderByIdDesc();
    List<Product> findTop4ByBrandNameIgnoreCase(String brandName);
    List<Product> findByCategoryNameContainingIgnoreCase(String categoryName);
    List<Product> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryName IS NULL OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:materialId IS NULL OR p.material.id = :materialId) AND " +
            "(:movementTypeId IS NULL OR p.movementType.id = :movementTypeId) AND " +
            "(p.originalPrice >= :minPrice) AND " +
            "(p.originalPrice <= :maxPrice)")

    List<Product> filterProducts(@Param("categoryName") String categoryName,
                                 @Param("brandId") Integer brandId,
                                 @Param("materialId") Integer materialId,
                                 @Param("movementTypeId") Integer movementTypeId,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice);
}