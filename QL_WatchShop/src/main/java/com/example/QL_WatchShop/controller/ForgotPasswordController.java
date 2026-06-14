package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "web/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgot(@RequestParam("email") String email, HttpSession session, RedirectAttributes ra) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy tài khoản nào đăng ký với Email này!");
            return "redirect:/forgot-password";
        }

        session.setAttribute("resetEmail", email);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(HttpSession session, Model model) {
        if (session.getAttribute("resetEmail") == null) {
            return "redirect:/forgot-password";
        }
        return "web/reset-password";
    }

    @PostMapping("/reset-password")
    public String processReset(@RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               HttpSession session, RedirectAttributes ra) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) return "redirect:/forgot-password";

        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/reset-password";
        }

        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        session.removeAttribute("resetEmail");
        ra.addFlashAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        return "redirect:/login";
    }
}