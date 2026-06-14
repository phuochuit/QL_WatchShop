package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Order;
import com.example.QL_WatchShop.repository.OrderDetailRepository;
import com.example.QL_WatchShop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;

    @GetMapping("")
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("orders", orders);
        return "admin/order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Integer id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return "redirect:/admin/order";

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetailRepository.findByOrder(order));
        return "admin/order/detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Integer id,
                               @RequestParam("status") String status,
                               RedirectAttributes ra) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderRepository.save(order);
            ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng #" + id);
        }
        return "redirect:/admin/order/" + id;
    }
}