package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Order;
import com.example.QL_WatchShop.model.OrderDetail;
import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.OrderDetailRepository;
import com.example.QL_WatchShop.repository.OrderRepository;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderHistoryController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/user/orders")
    public String viewOrderHistory(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String loginName = principal.getName();
        User user = userRepository.findByFullName(loginName);

        if (user != null) {
            List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);

            Map<Integer, List<OrderDetail>> orderDetailsMap = new HashMap<>();
            for (Order order : orders) {
                orderDetailsMap.put(order.getId(), orderDetailRepository.findByOrder(order));
            }

            model.addAttribute("orders", orders);
            model.addAttribute("orderDetailsMap", orderDetailsMap);
        }

        return "web/order-history";
    }

    @PostMapping("/user/orders/cancel")
    public String cancelOrder(@RequestParam("orderId") Integer orderId,
                              Principal principal,
                              RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null) {
            if (order.getUser() != null && order.getUser().getFullName().equals(principal.getName())) {

                if ("PENDING".equals(order.getStatus())) {
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    ra.addFlashAttribute("successMessage", "✅ Đã hủy thành công đơn hàng #" + orderId);
                } else {
                    ra.addFlashAttribute("errorMessage", "⚠️ Lỗi: Không thể hủy đơn hàng đã được xử lý hoặc giao đi!");
                }
            } else {
                ra.addFlashAttribute("errorMessage", "⚠️ Lỗi: Bạn không có quyền hủy đơn hàng này!");
            }
        }

        return "redirect:/user/orders";
    }
}