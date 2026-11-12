package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.ProductResponse;
import com.example.orderservice.model.Order;
import com.example.orderservice.web.CreateOrderRequest;
import com.example.orderservice.web.OrderResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final ProductClient productClient;
    private final List<Order> orders = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    /**
     * Validates the product against product-service, then stores the order.
     *
     * @throws ResponseStatusException 400 when the product does not exist,
     *                                 503 when product-service cannot be reached
     */
    public OrderResponse create(CreateOrderRequest request) {
        ProductResponse product = fetchProduct(request.productId());

        Order order = new Order(sequence.incrementAndGet(), product.id(), request.quantity());
        orders.add(order);

        BigDecimal total = product.price().multiply(BigDecimal.valueOf(order.quantity()));
        return new OrderResponse(
                order.id(),
                order.productId(),
                product.name(),
                order.quantity(),
                product.price(),
                total);
    }

    public List<Order> findAll() {
        return List.copyOf(orders);
    }

    private ProductResponse fetchProduct(Long productId) {
        try {
            return productClient.findById(productId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Product %d does not exist".formatted(productId), e);
        } catch (FeignException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "product-service is unavailable", e);
        }
    }
}
