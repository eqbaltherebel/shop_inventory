package com.shop_inventory.controller;

import com.shop_inventory.dto.request.LocationRequest;
import com.shop_inventory.dto.response.LocationResponse;
import com.shop_inventory.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // GET /api/locations
    @GetMapping
    public List<LocationResponse> getAll() {
        return locationService.findAll();
    }

    // GET /api/locations/{id}
    @GetMapping("/{id}")
    public LocationResponse getById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    // GET /api/locations/aisle/{aisle}  → e.g. /api/locations/aisle/A
    @GetMapping("/aisle/{aisle}")
    public List<LocationResponse> getByAisle(@PathVariable String aisle) {
        return locationService.findByAisle(aisle);
    }

    // POST /api/locations
    @PostMapping
    public ResponseEntity<LocationResponse> create(
            @RequestBody @Valid LocationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationService.save(request));
    }

    // PUT /api/locations/{id}
    @PutMapping("/{id}")
    public LocationResponse update(
            @PathVariable Long id,
            @RequestBody @Valid LocationRequest request) {
        return locationService.update(id, request);
    }

    // DELETE /api/locations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
