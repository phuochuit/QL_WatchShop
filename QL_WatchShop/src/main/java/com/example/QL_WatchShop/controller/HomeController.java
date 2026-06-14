package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Brand;
import com.example.QL_WatchShop.model.Product;
import com.example.QL_WatchShop.model.Review; // Nhớ import Review
import com.example.QL_WatchShop.repository.BrandRepository;
import com.example.QL_WatchShop.repository.MaterialRepository;
import com.example.QL_WatchShop.repository.MovementTypeRepository;
import com.example.QL_WatchShop.repository.ProductRepository;
import com.example.QL_WatchShop.repository.ReviewRepository; // Nhớ import ReviewRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired private MaterialRepository materialRepository;
    @Autowired private MovementTypeRepository movementTypeRepository;

    @Autowired private ReviewRepository reviewRepository;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("hotProducts", productRepository.findTop4ByOrderByIdDesc());
        model.addAttribute("casioProducts", productRepository.findTop4ByBrandNameIgnoreCase("Casio"));
        model.addAttribute("hotBrands", brandRepository.findByIsHotTrue());
        return "web/home";
    }

    @GetMapping("/danh-muc/{categoryName}")
    public String showCategoryPage(
            @PathVariable String categoryName,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer movementTypeId,
            @RequestParam(required = false, defaultValue = "0") Double minPrice,
            @RequestParam(required = false, defaultValue = "50000000") Double maxPrice,
            Model model) {

        List<Product> filteredProducts = productRepository.filterProducts(
                categoryName, brandId, materialId, movementTypeId, minPrice, maxPrice);

        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("movementTypes", movementTypeRepository.findAll());

        model.addAttribute("currentCategory", categoryName);
        model.addAttribute("currentBrandId", brandId);
        model.addAttribute("currentMaterialId", materialId);
        model.addAttribute("currentMovementTypeId", movementTypeId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("products", filteredProducts);
        model.addAttribute("pageTitle", "ĐỒNG HỒ " + categoryName.toUpperCase());
        model.addAttribute("submitUrl", "/danh-muc/" + categoryName);

        return "web/category";
    }

    @GetMapping("/thuong-hieu/{brandName}")
    public String showBrandPage(
            @PathVariable String brandName,
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer movementTypeId,
            @RequestParam(required = false, defaultValue = "0") Double minPrice,
            @RequestParam(required = false, defaultValue = "50000000") Double maxPrice,
            Model model) {

        Brand brand = brandRepository.findByNameIgnoreCase(brandName);
        Integer brandId = (brand != null) ? brand.getId() : null;

        List<Product> filteredProducts = productRepository.filterProducts(
                null, brandId, materialId, movementTypeId, minPrice, maxPrice);

        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("movementTypes", movementTypeRepository.findAll());

        model.addAttribute("currentCategory", brandName.toUpperCase());
        model.addAttribute("currentBrandId", brandId);
        model.addAttribute("currentMaterialId", materialId);
        model.addAttribute("currentMovementTypeId", movementTypeId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        model.addAttribute("submitUrl", "/thuong-hieu/" + brandName);

        model.addAttribute("hideBrandFilter", true);

        model.addAttribute("products", filteredProducts);
        model.addAttribute("pageTitle", "ĐỒNG HỒ " + brandName.toUpperCase() + " CHÍNH HÃNG");

        return "web/category";
    }

    @GetMapping("/san-pham/{id}")
    public String showProductDetail(@PathVariable("id") Integer id, Model model) {
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return "redirect:/";
        }

        List<Review> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);

        int totalReviews = reviews.size();
        double averageRating = 0.0;

        if (totalReviews > 0) {
            double sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            averageRating = sum / totalReviews;
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("averageRating", averageRating);

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Đồng hồ " + product.getBrand().getName() + " " + product.getName());

        return "web/product-detail";
    }

    @GetMapping("/tim-kiem")
    public String searchProducts(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm cho: " + keyword);

        return "web/search-results";
    }
}