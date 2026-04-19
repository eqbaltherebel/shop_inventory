package com.shop_inventory.service;

import com.shop_inventory.dto.request.CustomerRequest;
import com.shop_inventory.dto.response.CustomerResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.Customer;
import com.shop_inventory.model.Sale;
import com.shop_inventory.repository.CustomerRepository;
import com.shop_inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final SaleService saleService;

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    public CustomerResponse findById(Long id) {
        Customer customer = getOrThrow(id);
        return toResponse(customer, true); // include full sale history
    }

    public List<CustomerResponse> search(String query) {
        return customerRepository.search(query)
                .stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    public CustomerResponse create(CustomerRequest request) {
        // Check duplicate phone
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customerRepository.findByPhone(request.getPhone())
                    .ifPresent(c -> {
                        throw new IllegalStateException(
                                "Customer with phone " + request.getPhone()
                                        + " already exists");
                    });
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());

        return toResponse(customerRepository.save(customer), false);
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getOrThrow(id);
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        return toResponse(customerRepository.save(customer), false);
    }

    public void delete(Long id) {
        customerRepository.delete(getOrThrow(id));
    }

    // Get full purchase history for a customer
    public CustomerResponse getWithSales(Long id) {
        return toResponse(getOrThrow(id), true);
    }

    // ── Helpers ───────────────────────────────────────────────

    private Customer getOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + id));
    }

    public CustomerResponse toResponse(
            Customer customer, boolean includeSales) {

        List<Sale> sales = saleRepository
                .findByCustomerIdOrderBySaleDateDesc(customer.getId());

        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setName(customer.getName());
        res.setPhone(customer.getPhone());
        res.setAddress(customer.getAddress());
        res.setEmail(customer.getEmail());
        res.setCreatedAt(customer.getCreatedAt());
        res.setTotalPurchases(sales.size());
        res.setTotalSpent(
                sales.stream()
                        .filter(s -> "COMPLETED".equals(s.getStatus().name()))
                        .mapToDouble(Sale::getTotalAmount).sum());
        res.setTotalProfit(
                sales.stream()
                        .filter(s -> "COMPLETED".equals(s.getStatus().name()))
                        .mapToDouble(Sale::getProfit).sum());
        res.setLastPurchase(
                sales.stream()
                        .max(Comparator.comparing(Sale::getSaleDate))
                        .map(Sale::getSaleDate).orElse(null));

        if (includeSales) {
            res.setSales(sales.stream()
                    .map(saleService::toPublicResponse)
                    .collect(Collectors.toList()));
        }

        return res;
    }
}