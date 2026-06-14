package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user/list";
    }

    @PostMapping("/toggle-status")
    public String toggleStatus(@RequestParam("id") Integer id, Principal principal, RedirectAttributes ra) {
        User targetUser = userRepository.findById(id).orElse(null);

        if (targetUser != null) {
            if (principal != null && principal.getName().equals(targetUser.getFullName())) {
                ra.addFlashAttribute("errorMessage", "⚠️ Lỗi: Bạn không thể tự khóa tài khoản của chính mình!");
                return "redirect:/admin/user";
            }

            targetUser.setActive(!targetUser.getIsActive());
            userRepository.save(targetUser);

            String action = targetUser.getIsActive() ? "MỞ KHÓA" : "KHÓA";
            ra.addFlashAttribute("successMessage", "✅ Đã " + action + " thành công tài khoản: " + targetUser.getFullName());
        }
        return "redirect:/admin/user";
    }
}