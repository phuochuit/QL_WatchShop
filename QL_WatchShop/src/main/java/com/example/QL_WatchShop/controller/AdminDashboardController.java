package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Order;
import com.example.QL_WatchShop.repository.OrderRepository;
import com.example.QL_WatchShop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {
        java.time.LocalDateTime start = java.time.LocalDateTime.of(1, 1, 1, 0, 0);
        java.time.LocalDateTime end = java.time.LocalDateTime.now();

        Double totalRevenue = orderRepository.calculateTotalRevenue(start, end);

        Double totalDiscount = orderRepository.calculateTotalDiscount(start, end);

        long newOrdersCount = orderRepository.countByStatus("PENDING");

        long totalProducts = productRepository.count();

        List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .toList();

        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("totalDiscount", totalDiscount != null ? totalDiscount : 0.0);
        model.addAttribute("newOrdersCount", newOrdersCount);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }
}