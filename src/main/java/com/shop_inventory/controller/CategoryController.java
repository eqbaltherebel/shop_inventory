package com.shop_inventory.controller;

import com.shop_inventory.dto.request.CategoryRequest;
import com.shop_inventory.dto.response.CategoryResponse;
import com.shop_inventory.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories
    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.findAll();
    }

    // GET /api/categories/{id}
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    // POST /api/categories
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @RequestBody @Valid CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.save(request));
    }

    // PUT /api/categories/{id}
    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest request) {
        return categoryService.update(id, request);
    }

    // DELETE /api/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
