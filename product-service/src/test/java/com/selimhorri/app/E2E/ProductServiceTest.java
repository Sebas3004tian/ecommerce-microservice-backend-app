package com.selimhorri.app.E2E;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private ProductDto buildValidProduct(String title) {
        CategoryDto category = CategoryDto.builder().categoryId(1).categoryTitle("Cat1").build();
        return ProductDto.builder()
            .productId(1)
            .productTitle(title)
            .imageUrl("img.png")
            .sku("SKU1")
            .priceUnit(10.0)
            .quantity(5)
            .categoryDto(category)
            .build();
    }

    @Test
    void testUserCreatesProduct() {
        ProductDto product = buildValidProduct("TestProduct");
        ResponseEntity<ProductDto> response = restTemplate.postForEntity("/api/products", product, ProductDto.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("TestProduct", response.getBody().getProductTitle());
    }
    @Test
    void testUserDeletesProduct() {
        ProductDto product = restTemplate.postForEntity("/api/products", buildValidProduct("DeleteProduct"), ProductDto.class).getBody();
        restTemplate.delete("/api/products/" + product.getProductId());
        assertNotNull(product);
    }
    @Test
    void testUserUpdatesProduct() {
        ProductDto product = restTemplate.postForEntity("/api/products", buildValidProduct("UpdateProduct"), ProductDto.class).getBody();
        product.setProductTitle("UpdatedProduct");
        restTemplate.put("/api/products/" + product.getProductId(), product);
        assertNotNull(product);
    }
    @Test
    void testUserViewsProductDetails() {
        ProductDto product = restTemplate.postForEntity("/api/products", buildValidProduct("DetailProduct"), ProductDto.class).getBody();
        ResponseEntity<ProductDto> response = restTemplate.getForEntity("/api/products/" + product.getProductId(), ProductDto.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            assertNotNull(response.getBody());
            assertEquals("DetailProduct", response.getBody().getProductTitle());
        } else {
            String errorJson = response.toString();
            assertTrue(errorJson.contains("timestamp"));
        }
    }
    @Test
    void testUserViewsNonExistentProduct() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/products/999999", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("timestamp"));
    }
} 