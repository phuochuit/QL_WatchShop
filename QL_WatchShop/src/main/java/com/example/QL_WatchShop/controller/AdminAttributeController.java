package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.*;
import com.example.QL_WatchShop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/attributes")
public class AdminAttributeController {

    @Autowired private CategoryService categoryService;
    @Autowired private BrandService brandService;
    @Autowired private MovementTypeService movementTypeService;
    @Autowired private MaterialService materialService;

    @GetMapping
    public String showManageAttributesPage(Model model) {
        return "admin/attribute/manage";
    }

    @PostMapping("/category/add")
    public String addCategory(@RequestParam("name") String name) {
        Category category = new Category();
        category.setName(name);
        categoryService.saveCategory(category);
        return "redirect:/admin/attributes?success";
    }

    @PostMapping("/brand/add")
    public String addBrand(@RequestParam("name") String name,
                           @RequestParam(value = "isHot", defaultValue = "false") Boolean isHot) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setHot(isHot);
        brandService.saveBrand(brand);
        return "redirect:/admin/attributes?success";
    }

    @PostMapping("/movement/add")
    public String addMovementType(@RequestParam("name") String name) {
        MovementType movementType = new MovementType();
        movementType.setName(name);
        movementTypeService.saveMovementType(movementType);
        return "redirect:/admin/attributes?success";
    }

    @PostMapping("/material/add")
    public String addMaterial(@RequestParam("name") String name) {
        Material material = new Material();
        material.setName(name);
        materialService.saveMaterial(material);
        return "redirect:/admin/attributes?success";
    }
}