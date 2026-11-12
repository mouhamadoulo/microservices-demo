package com.example.orderservice.web;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice) {
}
