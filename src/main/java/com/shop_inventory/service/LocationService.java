package com.shop_inventory.service;

import com.shop_inventory.dto.request.LocationRequest;
import com.shop_inventory.dto.response.LocationResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.Location;
import com.shop_inventory.repository.ItemRepository;
import com.shop_inventory.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final ItemRepository itemRepository;

    public List<LocationResponse> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<LocationResponse> findByAisle(String aisle) {
        return locationRepository.findByAisle(aisle)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse save(LocationRequest request) {
        Location location = toEntity(request, new Location());
        return toResponse(locationRepository.save(location));
    }

    public LocationResponse update(Long id, LocationRequest request) {
        Location location = toEntity(request, getOrThrow(id));
        return toResponse(locationRepository.save(location));
    }

    public void delete(Long id) {
        Location location = getOrThrow(id);

        // Check if any items are still assigned to this location
        int itemCount = itemRepository.findByLocationId(id).size();
        if (itemCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete location — " + itemCount + " item(s) are still stored here. " +
                            "Please reassign them first."
            );
        }

        locationRepository.delete(location);
    }

    // ── Helpers ──────────────────────────────────────────────

    private Location getOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id: " + id));
    }

    private Location toEntity(LocationRequest req, Location location) {
        location.setAisle(req.getAisle().toUpperCase());   // store as "A", "B"
        location.setRack(req.getRack());
        location.setShelf(req.getShelf());
        location.setNotes(req.getNotes());
        return location;
    }

    private LocationResponse toResponse(Location location) {
        LocationResponse res = new LocationResponse();
        res.setId(location.getId());
        res.setAisle(location.getAisle());
        res.setRack(location.getRack());
        res.setShelf(location.getShelf());
        res.setNotes(location.getNotes());
        res.setDisplay("Aisle " + location.getAisle()
                + " · Rack " + location.getRack()
                + " · " + location.getShelf());

        // Count items stored at this location
        res.setItemCount(itemRepository.findByLocationId(location.getId()).size());
        return res;
    }
}
