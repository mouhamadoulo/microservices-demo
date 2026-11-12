package com.example.productservice.model;

import java.math.BigDecimal;

/**
 * A product in the catalogue.
 */
public record Product(Long id, String name, BigDecimal price) {
}
