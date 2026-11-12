package com.example.orderservice.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull(message = "productId is required") Long productId,
        @Positive(message = "quantity must be greater than 0") int quantity) {
}
