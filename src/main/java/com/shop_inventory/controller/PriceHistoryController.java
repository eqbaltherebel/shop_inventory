package com.shop_inventory.controller;

import com.shop_inventory.model.PriceHistory;
import com.shop_inventory.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/price-history")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;

    @GetMapping("/{itemId}")
    public List<PriceHistory> getHistory(@PathVariable Long itemId) {
        System.out.println("In Price History");
        return priceHistoryRepository.findByItemIdOrderByChangedAtDesc(itemId);
    }
}
