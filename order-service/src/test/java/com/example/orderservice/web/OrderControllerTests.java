package com.example.orderservice.web;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.ProductResponse;
import com.example.orderservice.service.OrderService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(OrderService.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductClient productClient;

    @Test
    void createsAnOrderAndPricesItFromProductService() throws Exception {
        given(productClient.findById(2L))
                .willReturn(new ProductResponse(2L, "27\" 4K monitor", new BigDecimal("349.00")));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 2, "quantity": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.unitPrice").value(349.00))
                .andExpect(jsonPath("$.totalPrice").value(1047.00));
    }

    @Test
    void rejectsAnOrderForAnUnknownProduct() throws Exception {
        given(productClient.findById(any())).willThrow(notFound());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 99, "quantity": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsANonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 1, "quantity": 0}
                                """))
                .andExpect(status().isBadRequest());
    }

    private static FeignException.NotFound notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/products/99",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, Map.of());
    }
}
