package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "web/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "web/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, Model model) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "web/register";
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("error", "Số điện thoại này đã được đăng ký!");
            return "web/register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRole("USER");
        user.setActive(true);

        userRepository.save(user);

        return "redirect:/login?registerSuccess";
    }
}