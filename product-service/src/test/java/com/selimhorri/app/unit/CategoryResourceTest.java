package com.selimhorri.app.unit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.service.CategoryService;
import com.selimhorri.app.resource.CategoryResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

public class CategoryResourceTest {
     @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryResource categoryResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_ShouldReturnCategory() {
        CategoryDto mockCategory = new CategoryDto();
        when(categoryService.findById(1)).thenReturn(mockCategory);

        ResponseEntity<CategoryDto> response = categoryResource.findById("1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockCategory, response.getBody());
        verify(categoryService).findById(1);
    }

    @Test
    void save_ShouldReturnSavedCategory() {
        CategoryDto input = new CategoryDto();
        CategoryDto saved = new CategoryDto();
        when(categoryService.save(input)).thenReturn(saved);

        ResponseEntity<CategoryDto> response = categoryResource.save(input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(saved, response.getBody());
        verify(categoryService).save(input);
    }

    @Test
    void update_ShouldReturnUpdatedCategory() {
        CategoryDto input = new CategoryDto();
        CategoryDto updated = new CategoryDto();
        when(categoryService.update(input)).thenReturn(updated);

        ResponseEntity<CategoryDto> response = categoryResource.update(input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updated, response.getBody());
        verify(categoryService).update(input);
    }

    @Test
    void updateById_ShouldReturnUpdatedCategory() {
        CategoryDto input = new CategoryDto();
        CategoryDto updated = new CategoryDto();
        when(categoryService.update(eq(1), eq(input))).thenReturn(updated);

        ResponseEntity<CategoryDto> response = categoryResource.update("1", input);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updated, response.getBody());
        verify(categoryService).update(1, input);
    }

    @Test
    void deleteById_ShouldReturnTrue() {
        doNothing().when(categoryService).deleteById(1);

        ResponseEntity<Boolean> response = categoryResource.deleteById("1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
        verify(categoryService).deleteById(1);
    }
}
