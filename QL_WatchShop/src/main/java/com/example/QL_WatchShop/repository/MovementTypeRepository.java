package com.example.QL_WatchShop.repository;

import com.example.QL_WatchShop.model.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementTypeRepository extends JpaRepository<MovementType, Integer> {
}