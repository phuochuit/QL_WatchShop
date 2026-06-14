package com.example.QL_WatchShop.service;

import com.example.QL_WatchShop.model.Material;
import com.example.QL_WatchShop.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {
    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    public void saveMaterial(Material material) {
        materialRepository.save(material);
    }
}