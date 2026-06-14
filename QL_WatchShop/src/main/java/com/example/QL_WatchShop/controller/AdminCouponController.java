package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Coupon;
import com.example.QL_WatchShop.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping("")
    public String listCoupons(Model model) {
        List<Coupon> coupons = couponRepository.findAll();
        model.addAttribute("coupons", coupons);
        model.addAttribute("activePage", "coupons");
        return "admin/coupon/list";
    }

    @PostMapping("/add")
    public String addCoupon(@RequestParam(value = "code", required = false) String code,
                            @RequestParam("discountType") String discountType,
                            @RequestParam("discountValue") Double discountValue,
                            @RequestParam(value = "minOrderAmount", required = false, defaultValue = "0") Double minOrderAmount,
                            @RequestParam("expirationDate") LocalDate expirationDate,
                            RedirectAttributes ra) {

        if (code == null || code.trim().isEmpty()) {
            code = "KM-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        } else {
            code = code.trim().toUpperCase();
        }

        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setExpirationDate(expirationDate);
        coupon.setIsActive(true);

        couponRepository.save(coupon);
        ra.addFlashAttribute("successMessage", "✅ Đã tạo thành công mã: " + code);

        return "redirect:/admin/coupons";
    }

    @GetMapping("/toggle/{id}")
    public String toggleCoupon(@PathVariable Integer id, RedirectAttributes ra) {
        Coupon coupon = couponRepository.findById(id).orElse(null);
        if (coupon != null) {
            coupon.setIsActive(!coupon.getIsActive());
            couponRepository.save(coupon);
            ra.addFlashAttribute("successMessage", "Đã thay đổi trạng thái mã " + coupon.getCode());
        }
        return "redirect:/admin/coupons";
    }
}