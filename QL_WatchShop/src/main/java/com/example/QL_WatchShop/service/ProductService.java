package com.example.QL_WatchShop.service;

import com.example.QL_WatchShop.model.*;
import com.example.QL_WatchShop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepo;
    @Autowired private ProductImageRepository imageRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private BrandRepository brandRepo;
    @Autowired private MovementTypeRepository movementTypeRepo;
    @Autowired private MaterialRepository materialRepo;
    @Autowired private CloudinaryService cloudinaryService;

    @Transactional
    public void saveProductWithImages(Product product, Integer categoryId, Integer brandId,
                                      Integer movementTypeId, Integer materialId,
                                      MultipartFile mainImage, MultipartFile[] extraImages) throws IOException {

        Category category = categoryRepo.findById(categoryId).orElseThrow();
        Brand brand = brandRepo.findById(brandId).orElseThrow();
        MovementType movementType = movementTypeRepo.findById(movementTypeId).orElseThrow();
        Material material = materialRepo.findById(materialId).orElseThrow();

        product.setCategory(category);
        product.setBrand(brand);
        product.setMovementType(movementType);
        product.setMaterial(material);
        product.setStatus((byte) 1);

        Product savedProduct = productRepo.save(product);

        if (mainImage != null && !mainImage.isEmpty()) {
            saveSingleImage(mainImage, savedProduct, category, brand, true);
        }

        if (extraImages != null && extraImages.length > 0) {
            for (MultipartFile file : extraImages) {
                if (!file.isEmpty()) {
                    saveSingleImage(file, savedProduct, category, brand, false);
                }
            }
        }
    }

    private void saveSingleImage(MultipartFile file, Product savedProduct, Category category, Brand brand, boolean isPrimary) throws IOException {

        String imageUrl = cloudinaryService.uploadImage(file, category.getName(), brand.getName());

        String safeCategory = sanitizeFolderName(category.getName());
        String safeBrand = sanitizeFolderName(brand.getName());
        String localDir = "uploads/WatchShop/" + safeCategory + "/" + safeBrand + "/";
        Path uploadPath = Paths.get(localDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Không thể lưu file local: " + file.getOriginalFilename(), e);
        }

        String localUrl = "/" + localDir + file.getOriginalFilename();

        ProductImage img = new ProductImage();
        img.setImageUrl(imageUrl);
        img.setLocalImageUrl(localUrl);
        img.setProduct(savedProduct);
        img.setPrimary(isPrimary);

        imageRepo.save(img);
    }

    private String sanitizeFolderName(String value) {
        if (value == null) return "Unknown";
        String temp = value.replace("Đ", "D").replace("đ", "d");
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("\\s+", "-");
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(Integer id) {
        return productRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    @Transactional
    public void updateProduct(Integer id, Product productDetails, Integer categoryId, Integer brandId,
                              Integer movementTypeId, Integer materialId,
                              MultipartFile mainImage, MultipartFile[] extraImages,
                              List<Integer> deletedImageIds, List<Integer> imageOrder) throws IOException {

        Product existingProduct = getProductById(id);

        if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
            for (Integer imgId : deletedImageIds) {
                imageRepo.deleteById(imgId);
            }
            existingProduct.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
        }

        if (imageOrder != null && !imageOrder.isEmpty()) {
            for (int i = 0; i < imageOrder.size(); i++) {
                ProductImage img = imageRepo.findById(imageOrder.get(i)).orElse(null);
                if (img != null) {
                    img.setSortOrder(i);
                    imageRepo.save(img);
                }
            }
        }

        existingProduct.setName(productDetails.getName());
        existingProduct.setOriginalPrice(productDetails.getOriginalPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setCategory(categoryRepo.findById(categoryId).get());
        existingProduct.setBrand(brandRepo.findById(brandId).get());
        existingProduct.setMovementType(movementTypeRepo.findById(movementTypeId).get());
        existingProduct.setMaterial(materialRepo.findById(materialId).get());

        productRepo.save(existingProduct);

        if (mainImage != null && !mainImage.isEmpty()) {
            List<ProductImage> oldMainImages = existingProduct.getImages().stream()
                    .filter(img -> img.getPrimary() != null && img.getPrimary())
                    .collect(Collectors.toList());

            imageRepo.deleteAll(oldMainImages);
            existingProduct.getImages().removeAll(oldMainImages);

            saveSingleImage(mainImage, existingProduct, existingProduct.getCategory(), existingProduct.getBrand(), true);
        }

        if (extraImages != null && extraImages.length > 0) {
            for (MultipartFile file : extraImages) {
                if (!file.isEmpty()) {
                    saveSingleImage(file, existingProduct, existingProduct.getCategory(), existingProduct.getBrand(), false);
                }
            }
        }
    }
}