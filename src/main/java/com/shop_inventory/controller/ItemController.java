package com.shop_inventory.controller;

import com.shop_inventory.dto.request.ItemRequest;
import com.shop_inventory.dto.response.ItemResponse;
import com.shop_inventory.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> create(
            @RequestPart("item") @Valid ItemRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo)
            throws IOException {
        System.out.println("In create item method");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.save(request, photo));
    }

    // Update item — accepts multipart form with optional new photo
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ItemResponse update(
            @PathVariable Long id,
            @RequestPart("item") @Valid ItemRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo)
            throws IOException {
        return itemService.update(id, request, photo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Delete only the photo of an item
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        itemService.deletePhoto(id);
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
