package com.example.orderservice.client;

import java.math.BigDecimal;

/**
 * Projection of the product-service payload consumed by this service.
 */
public record ProductResponse(Long id, String name, BigDecimal price) {
}
