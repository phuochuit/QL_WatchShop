package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UserController {

    @Autowired private UserRepository userRepository;

    @GetMapping("/user/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Lấy User theo Full Name (tương ứng với principal.getName())
        User user = userRepository.findByFullName(principal.getName());
        model.addAttribute("user", user);
        return "web/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                Principal principal,
                                RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByFullName(principal.getName());
        if (user != null) {
            user.setFullName(fullName);
            user.setPhone(phone);
            userRepository.save(user);

            ra.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        }

        return "redirect:/user/profile";
    }
}