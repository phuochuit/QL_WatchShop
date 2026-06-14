package com.example.QL_WatchShop.controller;

import com.example.QL_WatchShop.model.*;
import com.example.QL_WatchShop.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private ShippingAddressRepository shippingAddressRepository;

    @PostMapping("/checkout/confirm")
    @Transactional
    public String confirmOrder(
            @RequestParam(value = "addressId", required = false) Integer addressId,
            @RequestParam(value = "newName", required = false) String newName,
            @RequestParam(value = "newPhone", required = false) String newPhone,
            @RequestParam(value = "newAddress", required = false) String newAddress,
            HttpSession session, Principal principal) {

        if (principal == null) return "redirect:/login";

        User user = userRepository.findByFullName(principal.getName());
        if (user == null) return "redirect:/login";

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        String finalAddressText = "";
        if (addressId != null && addressId > 0) {
            ShippingAddress sa = shippingAddressRepository.findById(addressId).orElse(null);
            if (sa != null) finalAddressText = sa.getReceiverName() + " (" + sa.getPhoneNumber() + ") - " + sa.getAddressDetail();
        } else {
            ShippingAddress sa = new ShippingAddress();
            sa.setUser(user);
            sa.setReceiverName(newName);
            sa.setPhoneNumber(newPhone);
            sa.setAddressDetail(newAddress);
            shippingAddressRepository.save(sa);
            finalAddressText = newName + " (" + newPhone + ") - " + newAddress;
        }

        double originalTotal = cart.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        double discountAmount = 0.0;
        String couponCode = null;

        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        if (appliedCoupon != null) {
            if ("PERCENT".equalsIgnoreCase(appliedCoupon.getDiscountType())) {
                discountAmount = originalTotal * (appliedCoupon.getDiscountValue() / 100.0);
            } else {
                discountAmount = appliedCoupon.getDiscountValue();
            }
            if (discountAmount > originalTotal) discountAmount = originalTotal;
            couponCode = appliedCoupon.getCode();
        }
        double finalTotal = originalTotal - discountAmount;

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(finalAddressText);
        order.setTotalAmount(java.math.BigDecimal.valueOf(finalTotal));
        order.setDiscountAmount(discountAmount);
        order.setCouponCode(couponCode);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentMethod("COD");
        orderRepository.save(order);

        for (CartItem item : cart) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);

            Product product = productRepository.findById(item.getProductId()).get();
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(java.math.BigDecimal.valueOf(item.getPrice()));

            orderDetailRepository.save(detail);

            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " đã hết hàng!");
            }
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }

        session.removeAttribute("cart");
        session.removeAttribute("totalQuantity");
        session.removeAttribute("totalPrice");
        session.removeAttribute("appliedCoupon");

        return "redirect:/checkout/success";
    }

    @GetMapping("/checkout/success")
    public String orderSuccess(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
            if (!orders.isEmpty()) {
                Order latestOrder = orders.get(0);
                model.addAttribute("order", latestOrder);
                model.addAttribute("orderDetails", orderDetailRepository.findByOrder(latestOrder));
            }
        }
        return "web/checkout-success";
    }
}