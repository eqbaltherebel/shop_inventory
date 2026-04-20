package com.shop_inventory.service;

import com.shop_inventory.dto.request.SaleItemRequest;
import com.shop_inventory.dto.request.SaleRequest;
import com.shop_inventory.dto.response.ReportResponse;
import com.shop_inventory.dto.response.SaleItemResponse;
import com.shop_inventory.dto.response.SaleResponse;
import com.shop_inventory.dto.response.TopItemResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.*;
import com.shop_inventory.repository.CustomerRepository;
import com.shop_inventory.repository.ItemRepository;
import com.shop_inventory.repository.SaleItemRepository;
import com.shop_inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter KEY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── Create Sale ───────────────────────────────────────────

    // ── Update createSale() to link customer ──────────────────────
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        Sale sale = new Sale();
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setNotes(request.getNotes());
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setInvoiceNumber(generateInvoiceNumber());

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        sale.setSoldBy(username);

        // ── Link or create customer ───────────────────────────────
        if (request.getCustomerId() != null) {
            // Link to existing customer
            Customer customer = customerRepository
                    .findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found"));
            sale.setCustomer(customer);
            sale.setCustomerName(customer.getName());
            sale.setCustomerPhone(customer.getPhone());

        } else if (request.getCustomerName() != null
                && !request.getCustomerName().isBlank()) {

            // Auto-create customer if phone matches existing one
            if (request.getCustomerPhone() != null
                    && !request.getCustomerPhone().isBlank()) {

                Customer customer = customerRepository
                        .findByPhone(request.getCustomerPhone())
                        .orElseGet(() -> {
                            Customer c = new Customer();
                            c.setName(request.getCustomerName());
                            c.setPhone(request.getCustomerPhone());
                            c.setAddress(request.getCustomerAddress());
                            return customerRepository.save(c);
                        });
                sale.setCustomer(customer);
            }

            sale.setCustomerName(request.getCustomerName());
            sale.setCustomerPhone(request.getCustomerPhone());
        }

        // ── Process items (same as before) ────────────────────────
        List<SaleItem> saleItems = new ArrayList<>();
        double totalAmount = 0;
        double totalCost   = 0;

        for (SaleItemRequest sir : request.getItems()) {
            Item item = itemRepository.findById(sir.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item not found: " + sir.getItemId()));

            if (item.getQuantity() < sir.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for '" + item.getName() +
                                "'. Available: " + item.getQuantity() +
                                ", Requested: " + sir.getQuantity());
            }

            double priceAtSale = item.getSellingPrice();
            double costAtSale  = item.getBuyingPrice();
            double subtotal    = priceAtSale * sir.getQuantity();
            double itemProfit  = (priceAtSale - costAtSale)
                    * sir.getQuantity();

            SaleItem si = new SaleItem();
            si.setSale(sale);
            si.setItem(item);
            si.setQuantitySold(sir.getQuantity());
            si.setPriceAtSale(priceAtSale);
            si.setCostAtSale(costAtSale);
            si.setSubtotal(subtotal);
            si.setProfit(itemProfit);
            saleItems.add(si);

            totalAmount += subtotal;
            totalCost   += costAtSale * sir.getQuantity();

            item.setQuantity(item.getQuantity() - sir.getQuantity());
            itemRepository.save(item);
        }

        sale.setTotalAmount(totalAmount);
        sale.setTotalCost(totalCost);
        sale.setProfit(totalAmount - totalCost);
        sale.setSaleItems(saleItems);

        return toPublicResponse(saleRepository.save(sale));
    }

    // ── Search sales by customer ───────────────────────────────────
    public List<SaleResponse> searchByCustomer(String query) {
        return saleRepository.searchByCustomer(query)
                .stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    // ── Make toResponse public so CustomerService can use it ──────
    public SaleResponse toPublicResponse(Sale sale) {
        return toResponse(sale);
    }

    // ── Get All Sales ─────────────────────────────────────────

    public List<SaleResponse> getAll() {
        return saleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Sale::getSaleDate).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get Sale by ID ────────────────────────────────────────

    public SaleResponse getById(Long id) {
        return toResponse(saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found: " + id)));
    }

    // ── Today's Report ────────────────────────────────────────

    public ReportResponse getTodayReport() {
        LocalDate today = LocalDate.now();
        return getReport(today.toString(), today.toString());
    }

    // ── Custom Date Range Report ──────────────────────────────

    public ReportResponse getReport(String fromStr, String toStr) {
        LocalDateTime from = LocalDate.parse(fromStr)
                .atStartOfDay();
        LocalDateTime to   = LocalDate.parse(toStr)
                .atTime(23, 59, 59);

        List<Sale> sales = saleRepository
                .findBySaleDateBetweenOrderBySaleDateDesc(from, to)
                .stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        ReportResponse report = new ReportResponse();
        report.setFromDate(LocalDate.parse(fromStr).format(DATE_FMT));
        report.setToDate(LocalDate.parse(toStr).format(DATE_FMT));
        report.setTotalSales(sales.size());

        double totalRevenue = sales.stream()
                .mapToDouble(Sale::getTotalAmount).sum();
        double totalCost    = sales.stream()
                .mapToDouble(Sale::getTotalCost).sum();
        double totalProfit  = sales.stream()
                .mapToDouble(Sale::getProfit).sum();

        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        report.setTotalProfit(totalProfit);
        report.setProfitMargin(totalRevenue > 0
                ? (totalProfit / totalRevenue) * 100 : 0);

        // Revenue by day
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        Map<String, Double> profitByDay  = new LinkedHashMap<>();
        Map<String, Integer> salesByDay  = new LinkedHashMap<>();

        sales.forEach(s -> {
            String key = s.getSaleDate().format(KEY_FMT);
            revenueByDay.merge(key, s.getTotalAmount(), Double::sum);
            profitByDay.merge(key, s.getProfit(), Double::sum);
            salesByDay.merge(key, 1, Integer::sum);
        });

        report.setRevenueByDay(revenueByDay);
        report.setProfitByDay(profitByDay);
        report.setSalesByDay(salesByDay);

        // Revenue by payment method
        Map<String, Double> revenueByPayment = new LinkedHashMap<>();
        sales.forEach(s -> revenueByPayment.merge(
                s.getPaymentMethod(), s.getTotalAmount(), Double::sum));
        report.setRevenueByPayment(revenueByPayment);

        // Top items
        Map<Long, TopItemResponse> topMap = new LinkedHashMap<>();
        sales.forEach(s -> s.getSaleItems().forEach(si -> {
            Long itemId = si.getItem().getId();
            TopItemResponse top = topMap.getOrDefault(itemId,
                    new TopItemResponse());
            top.setItemId(itemId);
            top.setItemName(si.getItem().getName());
            top.setItemPhotoUrl(si.getItem().getPhotoUrl());
            top.setTotalQuantitySold(
                    (top.getTotalQuantitySold() == null ? 0
                            : top.getTotalQuantitySold()) + si.getQuantitySold());
            top.setTotalRevenue(
                    (top.getTotalRevenue() == null ? 0
                            : top.getTotalRevenue()) + si.getSubtotal());
            top.setTotalProfit(
                    (top.getTotalProfit() == null ? 0
                            : top.getTotalProfit()) + si.getProfit());
            topMap.put(itemId, top);
        }));

        List<TopItemResponse> topItems = topMap.values().stream()
                .sorted(Comparator.comparingInt(
                        TopItemResponse::getTotalQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());

        report.setTopItems(topItems);
        report.setSales(sales.stream()
                .map(this::toResponse).collect(Collectors.toList()));

        return report;
    }

    // ── Cancel Sale — restores inventory ─────────────────────

    @Transactional
    public SaleResponse cancelSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sale not found: " + id));

        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed sales can be cancelled");
        }

        // Restore inventory
        sale.getSaleItems().forEach(si -> {
            Item item = si.getItem();
            item.setQuantity(item.getQuantity() + si.getQuantitySold());
            itemRepository.save(item);
        });

        sale.setStatus(SaleStatus.CANCELLED);
        return toResponse(saleRepository.save(sale));
    }

    // ── Helpers ───────────────────────────────────────────────

    private String generateInvoiceNumber() {
        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = saleRepository.count() + 1;
        return String.format("INV-%s-%03d", datePart, count);
    }

    // ── Update toResponse() to include customer fields
    private SaleResponse toResponse(Sale sale) {
        SaleResponse res = new SaleResponse();
        res.setId(sale.getId());
        res.setInvoiceNumber(sale.getInvoiceNumber());
        res.setTotalAmount(sale.getTotalAmount());
        res.setTotalCost(sale.getTotalCost());
        res.setProfit(sale.getProfit());
        res.setPaymentMethod(sale.getPaymentMethod());
        res.setNotes(sale.getNotes());
        res.setStatus(sale.getStatus().name());
        res.setSoldBy(sale.getSoldBy());
        res.setSaleDate(sale.getSaleDate());

        // Customer fields
        if (sale.getCustomer() != null) {
            res.setCustomerId(sale.getCustomer().getId());
            res.setCustomerName(sale.getCustomer().getName());
            res.setCustomerPhone(sale.getCustomer().getPhone());
            res.setCustomerAddress(sale.getCustomer().getAddress());
        } else {
            res.setCustomerName(sale.getCustomerName());
            res.setCustomerPhone(sale.getCustomerPhone());
        }

        if (sale.getSaleItems() != null) {
            res.setSaleItems(sale.getSaleItems().stream()
                    .map(this::toSaleItemResponse)
                    .collect(Collectors.toList()));
        }

        return res;
    }

    private SaleItemResponse toSaleItemResponse(SaleItem si) {
        SaleItemResponse r = new SaleItemResponse();
        r.setId(si.getId());
        r.setItemId(si.getItem().getId());
        r.setItemName(si.getItem().getName());
        r.setItemPhotoUrl(si.getItem().getPhotoUrl());
        r.setQuantitySold(si.getQuantitySold());
        r.setPriceAtSale(si.getPriceAtSale());
        r.setCostAtSale(si.getCostAtSale());
        r.setSubtotal(si.getSubtotal());
        r.setProfit(si.getProfit());
        return r;
    }
}
