package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Product;
import com.example.QL_WatchShop.model.Review;
import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.ProductRepository;
import com.example.QL_WatchShop.repository.ReviewRepository;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ReviewController {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/product/review")
    public String submitReview(@RequestParam("productId") Integer productId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam("comment") String comment,
                               Principal principal,
                               RedirectAttributes ra) {

        if (principal == null) {
            ra.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đánh giá sản phẩm!");
            return "redirect:/login";
        }

        User user = userRepository.findByFullName(principal.getName());
        Product product = productRepository.findById(productId).orElse(null);

        if (user != null && product != null) {
            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(LocalDateTime.now());

            reviewRepository.save(review);
            ra.addFlashAttribute("successMessage", "Cảm ơn bạn đã để lại đánh giá!");
        }

        return "redirect:/san-pham/" + productId;
    }
}