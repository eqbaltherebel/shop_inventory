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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public List<ItemResponse> findAll() {
        return itemRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ItemResponse findById(Long id) {
        return toResponse(getItemOrThrow(id));
    }

    public ItemResponse save(ItemRequest request) {
        Item item = toEntity(request, new Item());
        return toResponse(itemRepository.save(item));
    }



    public void delete(Long id) {
        itemRepository.delete(getItemOrThrow(id));
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
        }
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(cat);
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
        res.setCreatedAt(item.getCreatedAt());
        res.setUpdatedAt(item.getUpdatedAt());

        if (item.getLocation() != null) {
            Location l = item.getLocation();
            res.setLocationDisplay("Aisle " + l.getAisle()
                    + " · Rack " + l.getRack()
                    + " · " + l.getShelf());
        }
        if (item.getCategory() != null) {
            res.setCategoryName(item.getCategory().getName());
        }
        return res;
    }

    // Update your update() method:
    public ItemResponse update(Long id, ItemRequest request) {
        Item existing = getItemOrThrow(id);

        // Record price history if price changed
        boolean priceChanged = !existing.getBuyingPrice().equals(request.getBuyingPrice())
                || !existing.getSellingPrice().equals(request.getSellingPrice());

        if (priceChanged) {
            PriceHistory history = new PriceHistory();
            history.setItem(existing);
            history.setBuyingPrice(request.getBuyingPrice());
            history.setSellingPrice(request.getSellingPrice());

            // Get current logged-in user
            String username = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            history.setChangedBy(username);
            priceHistoryRepository.save(history);
        }

        Item updated = toEntity(request, existing);
        return toResponse(itemRepository.save(updated));
    }

}
