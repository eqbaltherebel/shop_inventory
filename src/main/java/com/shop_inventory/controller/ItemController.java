package com.shop_inventory.controller;

import com.shop_inventory.dto.request.ItemRequest;
import com.shop_inventory.dto.response.ItemResponse;
import com.shop_inventory.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemResponse> getAll() {
        System.out.println("In get items method");
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@RequestBody @Valid ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.save(request));
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id,
                               @RequestBody @Valid ItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@RequestParam String query) {
        return itemService.search(query);
    }

    @GetMapping("/low-stock")
    public List<ItemResponse> lowStock(
            @RequestParam(defaultValue = "5") int threshold) {
        return itemService.getLowStock(threshold);
    }

    @GetMapping("/by-category/{categoryId}")
    public List<ItemResponse> byCategory(@PathVariable Long categoryId) {
        return itemService.findByCategory(categoryId);
    }

    @GetMapping("/by-location/{locationId}")
    public List<ItemResponse> byLocation(@PathVariable Long locationId) {
        return itemService.findByLocation(locationId);
    }
}
