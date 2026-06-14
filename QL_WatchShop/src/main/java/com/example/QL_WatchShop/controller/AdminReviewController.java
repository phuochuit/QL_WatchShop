package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Review;
import com.example.QL_WatchShop.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("")
    public String listReviews(Model model) {
        List<Review> reviews = reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("reviews", reviews);
        return "admin/review/list";
    }

    @PostMapping("/delete")
    public String deleteReview(@RequestParam("id") Integer id, RedirectAttributes ra) {
        reviewRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "🗑️ Đã xóa đánh giá vi phạm thành công!");
        return "redirect:/admin/reviews";
    }
}