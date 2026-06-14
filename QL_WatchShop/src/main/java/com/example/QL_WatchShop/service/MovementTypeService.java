package com.example.QL_WatchShop.service;

import com.example.QL_WatchShop.model.MovementType;
import com.example.QL_WatchShop.repository.MovementTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovementTypeService {
    @Autowired
    private MovementTypeRepository movementTypeRepository;

    public List<MovementType> getAllMovementTypes() {
        return movementTypeRepository.findAll();
    }

    public void saveMovementType(MovementType movementType) {
        movementTypeRepository.save(movementType);
    }
}