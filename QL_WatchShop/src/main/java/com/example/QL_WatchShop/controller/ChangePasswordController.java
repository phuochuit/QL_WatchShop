package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ChangePasswordController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/user/change-password")
    public String showChangePasswordForm(Principal principal) {
        if (principal == null) return "redirect:/login";
        return "web/change-password";
    }

    @PostMapping("/user/change-password")
    public String processChangePassword(@RequestParam("oldPassword") String oldPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Principal principal,
                                        RedirectAttributes ra) {

        User user = userRepository.findByFullName(principal.getName());

        if (user != null) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                ra.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
                return "redirect:/user/change-password";
            }

            if (!newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("errorMessage", "Mật khẩu mới xác nhận không khớp!");
                return "redirect:/user/change-password";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        }

        return "redirect:/user/change-password";
    }
}