package com.example.productservice.service;

import com.example.productservice.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * In-memory catalogue. Replace with a real repository when persistence arrives.
 */
@Service
public class ProductService {

    private static final List<Product> PRODUCTS = List.of(
            new Product(1L, "Mechanical keyboard", new BigDecimal("129.90")),
            new Product(2L, "27\" 4K monitor", new BigDecimal("349.00")),
            new Product(3L, "Wireless mouse", new BigDecimal("49.50")),
            new Product(4L, "USB-C docking station", new BigDecimal("199.99")),
            new Product(5L, "Noise cancelling headset", new BigDecimal("279.00"))
    );

    public List<Product> findAll() {
        return PRODUCTS;
    }

    public Optional<Product> findById(Long id) {
        return PRODUCTS.stream()
                .filter(product -> product.id().equals(id))
                .findFirst();
    }
}
