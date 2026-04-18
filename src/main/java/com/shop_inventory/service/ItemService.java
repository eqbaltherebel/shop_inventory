package com.shop_inventory.service;

import com.shop_inventory.dto.request.ItemRequest;
import com.shop_inventory.dto.response.ItemResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.Category;
import com.shop_inventory.model.Item;
import com.shop_inventory.model.Location;
import com.shop_inventory.model.PriceHistory;
import com.shop_inventory.repository.CategoryRepository;
import com.shop_inventory.repository.ItemRepository;
import com.shop_inventory.repository.LocationRepository;
import com.shop_inventory.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final CloudinaryService cloudinaryService;

    // Base URL for photo access
    private static final String PHOTO_BASE_URL = "http://localhost:8080/uploads/items/";

    public List<ItemResponse> findAll() {
        return itemRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ItemResponse findById(Long id) {
        return toResponse(getItemOrThrow(id));
    }

    public ItemResponse save(ItemRequest request,
                             MultipartFile photo) throws IOException {
        Item item = toEntity(request, new Item());
        item = itemRepository.save(item); // save first to get ID

        if (photo != null && !photo.isEmpty()) {
            String url = cloudinaryService.uploadImage(photo, item.getId());
            String publicId = cloudinaryService.buildPublicId(item.getId());
            item.setPhotoUrl(url);
            item.setPhotoPublicId(publicId);
            item = itemRepository.save(item);
        }

        return toResponse(item);
    }


    public void deletePhoto(Long id) {
        Item item = getItemOrThrow(id);
        if (item.getPhotoPublicId() != null) {
            cloudinaryService.deleteImage(item.getPhotoPublicId());
        }
        item.setPhotoUrl(null);
        item.setPhotoPublicId(null);
        itemRepository.save(item);
    }

    public void delete(Long id) {
        Item item = getItemOrThrow(id);
        // Delete from Cloudinary before removing from DB
        if (item.getPhotoPublicId() != null) {
            cloudinaryService.deleteImage(item.getPhotoPublicId());
        }
        itemRepository.delete(item);
    }

    public List<ItemResponse> search(String query) {
        return itemRepository.searchItems(query)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ItemResponse> getLowStock(int threshold) {
        return itemRepository.findByQuantityLessThanEqual(threshold)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ItemResponse> findByCategory(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ItemResponse> findByLocation(Long locationId) {
        return itemRepository.findByLocationId(locationId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
    }

    private Item toEntity(ItemRequest req, Item item) {
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setQuantity(req.getQuantity());
        item.setBuyingPrice(req.getBuyingPrice());
        item.setSellingPrice(req.getSellingPrice());

        if (req.getLocationId() != null) {
            Location loc = locationRepository.findById(req.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
            item.setLocation(loc);
        } else {
            item.setLocation(null);
        }

        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(cat);
        } else {
            item.setCategory(null);
        }

        return item;
    }

    private ItemResponse toResponse(Item item) {
        ItemResponse res = new ItemResponse();
        res.setId(item.getId());
        res.setName(item.getName());
        res.setDescription(item.getDescription());
        res.setQuantity(item.getQuantity());
        res.setBuyingPrice(item.getBuyingPrice());
        res.setSellingPrice(item.getSellingPrice());
        res.setPhotoUrl(item.getPhotoUrl());           // direct Cloudinary URL
        res.setPhotoPublicId(item.getPhotoPublicId());
        res.setCreatedAt(item.getCreatedAt());
        res.setUpdatedAt(item.getUpdatedAt());

        if (item.getLocation() != null) {
            Location l = item.getLocation();
            res.setLocationDisplay("Aisle " + l.getAisle()
                    + " · Rack " + l.getRack()
                    + " · " + l.getShelf());
            res.setLocationId(l.getId());      // ← ADD
        }

        if (item.getCategory() != null) {
            res.setCategoryName(item.getCategory().getName());
            res.setCategoryId(item.getCategory().getId());  // ← ADD
        }

        return res;
    }

    public ItemResponse update(Long id, ItemRequest request,
                               MultipartFile photo) throws IOException {
        Item existing = getItemOrThrow(id);

        // Track price history if price changed
        boolean priceChanged =
                !existing.getBuyingPrice().equals(request.getBuyingPrice()) ||
                        !existing.getSellingPrice().equals(request.getSellingPrice());

        if (priceChanged) {
            PriceHistory history = new PriceHistory();
            history.setItem(existing);
            history.setBuyingPrice(request.getBuyingPrice());
            history.setSellingPrice(request.getSellingPrice());
            String username = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            history.setChangedBy(username);
            priceHistoryRepository.save(history);
        }

        // Upload new photo — Cloudinary auto-replaces old one
        // because we use the same public_id (item_<id>)
        if (photo != null && !photo.isEmpty()) {
            String url = cloudinaryService.uploadImage(photo, id);
            String publicId = cloudinaryService.buildPublicId(id);
            existing.setPhotoUrl(url);
            existing.setPhotoPublicId(publicId);
        }

        Item updated = toEntity(request, existing);
        return toResponse(itemRepository.save(updated));
    }


}
