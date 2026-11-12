package com.example.orderservice.model;

/**
 * An order placed for a single product.
 */
public record Order(Long id, Long productId, int quantity) {
}
