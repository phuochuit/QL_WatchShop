package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Product;
import com.example.QL_WatchShop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private MovementTypeService movementTypeService;
    @Autowired
    private MaterialService materialService;

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("movementTypes", movementTypeService.getAllMovementTypes());
        model.addAttribute("materials", materialService.getAllMaterials());

        return "admin/product/add";
    }

    @PostMapping("/add")
    public String processAddProduct(
            @ModelAttribute("product") Product product,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("brandId") Integer brandId,
            @RequestParam("movementTypeId") Integer movementTypeId,
            @RequestParam("materialId") Integer materialId,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages
    ) {
        try {
            productService.saveProductWithImages(product, categoryId, brandId, movementTypeId, materialId, mainImage, extraImages);
            return "redirect:/admin/product/add?success";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/product/add?error";
        }
    }

    @GetMapping("")
    public String showProductList(Model model) {
        List<Product> products = productService.getAllProducts();

        long outOfStockCount = products.stream().filter(p -> p.getStockQuantity() <= 0).count();

        if (outOfStockCount > 0) {
            model.addAttribute("outOfStockWarning", "⚠️ Cảnh báo: Hiện đang có " + outOfStockCount + " sản phẩm đã hết hàng. Vui lòng kiểm tra và nhập thêm!");
        }

        model.addAttribute("products", products);
        return "admin/product/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("movementTypes", movementTypeService.getAllMovementTypes());
        model.addAttribute("materials", materialService.getAllMaterials());

        return "admin/product/edit";
    }

    @PostMapping("/edit/{id}")
    public String processUpdate(@PathVariable("id") Integer id,
                                @ModelAttribute("product") Product product,
                                @RequestParam("categoryId") Integer categoryId,
                                @RequestParam("brandId") Integer brandId,
                                @RequestParam("movementTypeId") Integer movementTypeId,
                                @RequestParam("materialId") Integer materialId,
                                @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                                @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
                                @RequestParam(value = "deletedImageIds", required = false) List<Integer> deletedImageIds, // Hứng danh sách ảnh bị xóa
                                @RequestParam(value = "imageOrder", required = false) List<Integer> imageOrder // Hứng thứ tự mới
    ) {
        try {
            productService.updateProduct(id, product, categoryId, brandId, movementTypeId, materialId,
                    mainImage, extraImages, deletedImageIds, imageOrder);
            return "redirect:/admin/product?editSuccess";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/product/edit/" + id + "?error";
        }
    }
}