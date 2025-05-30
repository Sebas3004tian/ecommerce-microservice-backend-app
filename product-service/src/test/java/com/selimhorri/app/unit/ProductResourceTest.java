package com.selimhorri.app.unit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.service.ProductService;
import com.selimhorri.app.resource.ProductResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

public class ProductResourceTest {
    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductResource productResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Método auxiliar para crear un ProductDto con datos de ejemplo
    private ProductDto createExampleProductDto() {
        ProductDto dto = new ProductDto();
        dto.setProductId(1);
        dto.setProductTitle("Test Product");
        dto.setImageUrl("http://example.com/image.jpg");
        dto.setPriceUnit(99.99);
        // agregar más campos según definición de ProductDto
        return dto;
    }

    @Test
    void findById_ShouldReturnProduct() {
        ProductDto mockProduct = createExampleProductDto();
        when(productService.findById(1)).thenReturn(mockProduct);

        ResponseEntity<ProductDto> response = productResource.findById("1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        ProductDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getProductId());
        assertEquals("Test Product", responseBody.getProductTitle());
        assertEquals("http://example.com/image.jpg", responseBody.getImageUrl());
        assertEquals(99.99, responseBody.getPriceUnit());

        verify(productService).findById(1);
    }

    @Test
    void save_ShouldReturnSavedProduct() {
        ProductDto input = createExampleProductDto();
        ProductDto saved = createExampleProductDto();
        saved.setProductId(2); // simula que el guardado asigna id nuevo

        when(productService.save(input)).thenReturn(saved);

        ResponseEntity<ProductDto> response = productResource.save(input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        ProductDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.getProductId());
        assertEquals("Test Product", responseBody.getProductTitle());

        verify(productService).save(input);
    }

    @Test
    void update_ShouldReturnUpdatedProduct() {
        ProductDto input = createExampleProductDto();
        ProductDto updated = createExampleProductDto();
        updated.setProductTitle("Updated Product Name");

        when(productService.update(input)).thenReturn(updated);

        ResponseEntity<ProductDto> response = productResource.update(input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        ProductDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Updated Product Name", responseBody.getProductTitle());

        verify(productService).update(input);
    }

    @Test
    void updateById_ShouldReturnUpdatedProduct() {
        ProductDto input = createExampleProductDto();
        ProductDto updated = createExampleProductDto();
        updated.setProductTitle("Updated Product With ID");

        when(productService.update(eq(1), eq(input))).thenReturn(updated);

        ResponseEntity<ProductDto> response = productResource.update("1", input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        ProductDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Updated Product With ID", responseBody.getProductTitle());

        verify(productService).update(1, input);
    }

    @Test
    void deleteById_ShouldReturnTrue() {
        doNothing().when(productService).deleteById(1);

        ResponseEntity<Boolean> response = productResource.deleteById("1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());

        verify(productService).deleteById(1);
    }
}
