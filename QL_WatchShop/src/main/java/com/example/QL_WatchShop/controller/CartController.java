package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.Coupon;
import com.example.QL_WatchShop.model.ShippingAddress;
import com.example.QL_WatchShop.model.User;
import com.example.QL_WatchShop.repository.CouponRepository;
import com.example.QL_WatchShop.repository.ShippingAddressRepository;
import com.example.QL_WatchShop.repository.UserRepository;
import org.springframework.ui.Model;
import com.example.QL_WatchShop.model.CartItem;
import com.example.QL_WatchShop.model.Product;
import com.example.QL_WatchShop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CartController {
    @Autowired private ProductRepository productRepository;
    @Autowired private ShippingAddressRepository shippingAddressRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private CouponRepository couponRepository;

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Integer productId, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductId().equals(productId)).findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                String img = product.getImages().isEmpty() ? "" :
                        (product.getImages().get(0).getLocalImageUrl() != null ?
                                product.getImages().get(0).getLocalImageUrl() : product.getImages().get(0).getImageUrl());

                Double price = (product.getOriginalPrice() != null) ? product.getOriginalPrice().doubleValue() : 0.0;

                cart.add(new CartItem(productId, product.getName(), img, price, 1));
            }
        }

        session.setAttribute("cart", cart);
        session.setAttribute("totalQuantity", cart.stream().mapToInt(CartItem::getQuantity).sum());
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session, java.security.Principal principal) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        double totalPrice = 0;
        int totalQuantity = 0;
        if (cart != null) {
            totalPrice = cart.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            totalQuantity = cart.stream().mapToInt(CartItem::getQuantity).sum();
        }

        double discountAmount = 0;
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");

        if (appliedCoupon != null) {
            if (appliedCoupon.getMinOrderAmount() != null && totalPrice < appliedCoupon.getMinOrderAmount()) {
                session.removeAttribute("appliedCoupon");
                appliedCoupon = null;
            } else {
                if ("PERCENT".equalsIgnoreCase(appliedCoupon.getDiscountType())) {
                    discountAmount = totalPrice * (appliedCoupon.getDiscountValue() / 100.0);
                } else if ("FIXED".equalsIgnoreCase(appliedCoupon.getDiscountType())) {
                    discountAmount = appliedCoupon.getDiscountValue();
                }

                if (discountAmount > totalPrice) {
                    discountAmount = totalPrice;
                }
            }
        }

        double finalPrice = totalPrice - discountAmount;

        List<ShippingAddress> addresses = new ArrayList<>();
        if (principal != null) {
            User user = userRepository.findByFullName(principal.getName());
            if (user != null) {
                model.addAttribute("currentUser", user);
                addresses = shippingAddressRepository.findByUserId(user.getId());
            }
        }

        model.addAttribute("savedAddresses", addresses);
        model.addAttribute("cartItems", cart);

        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("appliedCoupon", appliedCoupon);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalPrice", finalPrice);

        return "web/cart";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam("code") String code, HttpSession session, RedirectAttributes ra) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code);

        if (coupon == null) {
            ra.addFlashAttribute("errorCoupon", "❌ Mã giảm giá không tồn tại hoặc đã bị khóa.");
            return "redirect:/cart";
        }

        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
            ra.addFlashAttribute("errorCoupon", "❌ Mã giảm giá này đã hết hạn sử dụng.");
            return "redirect:/cart";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("errorCoupon", "❌ Giỏ hàng trống, không thể áp dụng mã.");
            return "redirect:/cart";
        }

        double totalPrice = cart.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        if (coupon.getMinOrderAmount() != null && totalPrice < coupon.getMinOrderAmount()) {
            ra.addFlashAttribute("errorCoupon", "❌ Đơn hàng tối thiểu phải từ " + String.format("%,.0f", coupon.getMinOrderAmount()) + "đ để dùng mã này.");
            return "redirect:/cart";
        }

        session.setAttribute("appliedCoupon", coupon);
        ra.addFlashAttribute("successCoupon", "✅ Đã áp dụng mã giảm giá thành công!");
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute("appliedCoupon");
        ra.addFlashAttribute("successCoupon", "🗑️ Đã gỡ mã giảm giá.");
        return "redirect:/cart";
    }

    @GetMapping("/cart/increase/{id}")
    public String increaseCart(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        Product product = productRepository.findById(id).orElse(null);

        if (cart != null && product != null) {
            for (CartItem item : cart) {
                if (item.getProductId().equals(id)) {
                    if (item.getQuantity() + 1 > product.getStockQuantity()) {
                        ra.addFlashAttribute("errorMessage", "Số lượng trong kho chỉ còn còn lại " + product.getStockQuantity() + " sản phẩm.");
                    } else {
                        item.setQuantity(item.getQuantity() + 1);
                    }
                    break;
                }
            }
            updateSession(session, cart);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/decrease/{id}")
    public String decreaseCart(@PathVariable Integer id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getProductId().equals(id)) {
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    } else {
                        cart.remove(item);
                    }
                    break;
                }
            }
            updateSession(session, cart);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Integer id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProductId().equals(id));
            updateSession(session, cart);
        }
        return "redirect:/cart";
    }

    private void updateSession(HttpSession session, List<CartItem> cart) {
        int totalQuantity = cart.stream().mapToInt(CartItem::getQuantity).sum();
        session.setAttribute("cart", cart);
        session.setAttribute("totalQuantity", totalQuantity);
    }
}