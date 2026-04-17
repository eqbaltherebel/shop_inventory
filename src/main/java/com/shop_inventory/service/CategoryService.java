package com.shop_inventory.service;

import com.shop_inventory.dto.request.CategoryRequest;
import com.shop_inventory.dto.response.CategoryResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.Category;
import com.shop_inventory.repository.CategoryRepository;
import com.shop_inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public CategoryResponse save(CategoryRequest request) {
        // Check duplicate name
        categoryRepository.findByName(request.getName()).ifPresent(c -> {
            throw new IllegalStateException(
                    "Category '" + request.getName() + "' already exists.");
        });

        Category category = new Category();
        category.setName(request.getName());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);

        // Check duplicate name (exclude current)
        categoryRepository.findByName(request.getName()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new IllegalStateException(
                        "Category '" + request.getName() + "' already exists.");
            }
        });

        category.setName(request.getName());
        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = getOrThrow(id);

        // Block delete if items are using this category
        int itemCount = itemRepository.findByCategoryId(id).size();
        if (itemCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete category — " + itemCount + " item(s) are using it. " +
                            "Please reassign them first."
            );
        }

        categoryRepository.delete(category);
    }

    // ── Helpers ──────────────────────────────────────────────

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse res = new CategoryResponse();
        res.setId(category.getId());
        res.setName(category.getName());
        res.setItemCount(itemRepository.findByCategoryId(category.getId()).size());
        return res;
    }
}
