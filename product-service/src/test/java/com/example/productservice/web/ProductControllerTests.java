package com.example.productservice.web;

import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductService.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsTheWholeCatalogue() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mechanical keyboard"))
                .andExpect(jsonPath("$[0].price").value(129.90));
    }

    @Test
    void returnsASingleProduct() throws Exception {
        mockMvc.perform(get("/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Wireless mouse"));
    }

    @Test
    void returnsNotFoundForAnUnknownProduct() throws Exception {
        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }
}
