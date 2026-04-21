package com.shop_inventory.service;

import com.shop_inventory.dto.request.BorrowEntryRequest;
import com.shop_inventory.dto.request.BorrowPaymentRequest;
import com.shop_inventory.dto.response.BorrowEntryResponse;
import com.shop_inventory.dto.response.BorrowPaymentResponse;
import com.shop_inventory.dto.response.BorrowSummaryResponse;
import com.shop_inventory.dto.response.LedgerResponse;
import com.shop_inventory.exception.ResourceNotFoundException;
import com.shop_inventory.model.BorrowEntry;
import com.shop_inventory.model.BorrowPayment;
import com.shop_inventory.model.BorrowStatus;
import com.shop_inventory.model.Customer;
import com.shop_inventory.repository.BorrowEntryRepository;
import com.shop_inventory.repository.BorrowPaymentRepository;
import com.shop_inventory.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowEntryRepository borrowRepo;
    private final BorrowPaymentRepository paymentRepo;
    private final CustomerRepository customerRepo;

    // ── Create Borrow Entry ───────────────────────────────────

    @Transactional
    public BorrowEntryResponse createEntry(BorrowEntryRequest req) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + req.getCustomerId()));

        // Check credit limit
        if (customer.getCreditLimit() != null
                && customer.getCreditLimit() > 0) {
            double outstanding = borrowRepo
                    .findByCustomerIdAndDeletedFalse(customer.getId())
                    .stream()
                    .filter(b -> b.getStatus() != BorrowStatus.CLEARED
                            && b.getStatus() != BorrowStatus.WRITTEN_OFF)
                    .mapToDouble(BorrowEntry::getRemainingBalance)
                    .sum();
            if (outstanding + req.getTotalAmount()
                    > customer.getCreditLimit()) {
                throw new IllegalStateException(
                        "Credit limit exceeded! Limit: ₹"
                                + customer.getCreditLimit()
                                + ", Current outstanding: ₹" + outstanding
                                + ", Requested: ₹" + req.getTotalAmount());
            }
        }

        BorrowEntry entry = new BorrowEntry();
        entry.setCustomer(customer);
        entry.setTotalAmount(req.getTotalAmount());
        entry.setAmountPaid(0.0);
        entry.setRemainingBalance(req.getTotalAmount());
        entry.setBorrowDate(req.getBorrowDate());
        entry.setDueDate(req.getDueDate());
        entry.setDescription(req.getDescription());
        entry.setNotes(req.getNotes());
        entry.setTags(req.getTags());
        entry.setCreditLimit(req.getCreditLimit());
        entry.setStatus(BorrowStatus.PENDING);
        entry.setDeleted(false);
        entry.setPayments(new ArrayList<>());

        return toResponse(borrowRepo.save(entry));
    }

    // ── Update Borrow Entry ───────────────────────────────────

    @Transactional
    public BorrowEntryResponse updateEntry(
            Long id, BorrowEntryRequest req) {

        BorrowEntry entry = getActiveOrThrow(id);
        entry.setTotalAmount(req.getTotalAmount());
        entry.setBorrowDate(req.getBorrowDate());
        entry.setDueDate(req.getDueDate());
        entry.setDescription(req.getDescription());
        entry.setNotes(req.getNotes());
        entry.setTags(req.getTags());
        if (req.getCreditLimit() != null)
            entry.setCreditLimit(req.getCreditLimit());

        // Recalculate balance
        entry.setRemainingBalance(
                req.getTotalAmount() - entry.getAmountPaid());
        updateStatus(entry);

        return toResponse(borrowRepo.save(entry));
    }

    // ── Soft Delete Entry ─────────────────────────────────────

    @Transactional
    public void softDeleteEntry(Long id, String reason) {
        BorrowEntry entry = getActiveOrThrow(id);
        entry.setDeleted(true);
        entry.setDeletedAt(LocalDateTime.now());
        entry.setDeletedReason(reason);
        borrowRepo.save(entry);
    }

    // ── Add Payment ───────────────────────────────────────────

    @Transactional
    public BorrowEntryResponse addPayment(
            Long entryId, BorrowPaymentRequest req) {

        BorrowEntry entry = getActiveOrThrow(entryId);

        if (req.getAmount() > entry.getRemainingBalance()) {
            throw new IllegalStateException(
                    "Payment ₹" + req.getAmount()
                            + " exceeds remaining balance ₹"
                            + entry.getRemainingBalance());
        }

        BorrowPayment payment = new BorrowPayment();
        payment.setBorrowEntry(entry);
        payment.setAmount(req.getAmount());
        payment.setPaymentDate(req.getPaymentDate());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setNotes(req.getNotes());
        payment.setDeleted(false);

        paymentRepo.save(payment);

        // Update totals
        entry.setAmountPaid(entry.getAmountPaid() + req.getAmount());
        entry.setRemainingBalance(
                entry.getTotalAmount() - entry.getAmountPaid());
        updateStatus(entry);

        return toResponse(borrowRepo.save(entry));
    }

    // ── Edit Payment ──────────────────────────────────────────

    @Transactional
    public BorrowEntryResponse updatePayment(
            Long paymentId, BorrowPaymentRequest req) {

        BorrowPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId));

        BorrowEntry entry = payment.getBorrowEntry();
        double oldAmount = payment.getAmount();

        payment.setAmount(req.getAmount());
        payment.setPaymentDate(req.getPaymentDate());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setNotes(req.getNotes());
        paymentRepo.save(payment);

        // Recalculate from all active payments
        recalculate(entry);
        return toResponse(borrowRepo.save(entry));
    }

    // ── Soft Delete Payment ───────────────────────────────────

    @Transactional
    public BorrowEntryResponse deletePayment(Long paymentId) {
        BorrowPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId));

        payment.setDeleted(true);
        paymentRepo.save(payment);

        BorrowEntry entry = payment.getBorrowEntry();
        recalculate(entry);
        return toResponse(borrowRepo.save(entry));
    }

    // ── Get All ───────────────────────────────────────────────

    public List<BorrowEntryResponse> getAll() {
        return borrowRepo
                .findByDeletedFalseOrderByBorrowDateDesc()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BorrowEntryResponse getById(Long id) {
        return toResponse(getActiveOrThrow(id));
    }

    // ── Get by Customer ───────────────────────────────────────

    public List<BorrowEntryResponse> getByCustomer(Long customerId) {
        return borrowRepo
                .findByCustomerIdAndDeletedFalse(customerId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Search ────────────────────────────────────────────────

    public List<BorrowEntryResponse> search(String query) {
        return borrowRepo.searchByCustomer(query)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Filter by date range ──────────────────────────────────

    public List<BorrowEntryResponse> filterByDate(
            LocalDate from, LocalDate to) {
        return borrowRepo.findByDateRange(from, to)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Overdue entries ───────────────────────────────────────

    public List<BorrowEntryResponse> getOverdue() {
        return borrowRepo.findOverdue(LocalDate.now())
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Summary Dashboard ─────────────────────────────────────

    public BorrowSummaryResponse getSummary() {
        List<BorrowEntry> all = borrowRepo
                .findByDeletedFalseOrderByBorrowDateDesc();

        BorrowSummaryResponse summary = new BorrowSummaryResponse();
        summary.setTotalCreditGiven(
                borrowRepo.sumTotalCredit());
        summary.setTotalCollected(
                borrowRepo.sumTotalPaid());
        summary.setTotalOutstanding(
                borrowRepo.sumOutstanding());
        summary.setTotalEntries(all.size());
        summary.setPendingEntries((int) all.stream()
                .filter(b -> b.getStatus() == BorrowStatus.PENDING
                        || b.getStatus() == BorrowStatus.PARTIAL)
                .count());
        summary.setOverdueEntries((int) all.stream()
                .filter(b -> b.getDueDate() != null
                        && b.getDueDate().isBefore(LocalDate.now())
                        && b.getStatus() != BorrowStatus.CLEARED
                        && b.getStatus() != BorrowStatus.WRITTEN_OFF)
                .count());
        summary.setClearedEntries((int) all.stream()
                .filter(b -> b.getStatus() == BorrowStatus.CLEARED)
                .count());

        // Per-customer summary
        Map<Long, BorrowSummaryResponse.CustomerCreditSummary>
                custMap = new LinkedHashMap<>();

        all.forEach(b -> {
            Long cid = b.getCustomer().getId();
            BorrowSummaryResponse.CustomerCreditSummary cs =
                    custMap.getOrDefault(cid,
                            new BorrowSummaryResponse.CustomerCreditSummary());

            cs.setCustomerId(cid);
            cs.setCustomerName(b.getCustomer().getName());
            cs.setCustomerPhone(b.getCustomer().getPhone());
            cs.setTotalBorrowed((cs.getTotalBorrowed() == null
                    ? 0 : cs.getTotalBorrowed()) + b.getTotalAmount());
            cs.setTotalPaid((cs.getTotalPaid() == null
                    ? 0 : cs.getTotalPaid()) + b.getAmountPaid());
            cs.setOutstanding((cs.getOutstanding() == null
                    ? 0 : cs.getOutstanding())
                    + (b.getStatus() == BorrowStatus.CLEARED
                    || b.getStatus() == BorrowStatus.WRITTEN_OFF
                    ? 0 : b.getRemainingBalance()));

            if (b.getCustomer().getCreditLimit() != null
                    && b.getCustomer().getCreditLimit() > 0) {
                cs.setCreditLimit(b.getCustomer().getCreditLimit());
                cs.setCreditLimitExceeded(
                        cs.getOutstanding()
                                > b.getCustomer().getCreditLimit());
            }

            double outstanding = cs.getOutstanding() == null
                    ? 0 : cs.getOutstanding();
            if (outstanding <= 0) cs.setStatus("CLEAR");
            else if (b.getDueDate() != null
                    && b.getDueDate().isBefore(LocalDate.now()))
                cs.setStatus("OVERDUE");
            else cs.setStatus("PENDING");

            custMap.put(cid, cs);
        });

        summary.setCustomerSummaries(
                new ArrayList<>(custMap.values()));
        return summary;
    }

    // ── Ledger for a customer ─────────────────────────────────

    public LedgerResponse getLedger(Long customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found"));

        List<BorrowEntry> entries = borrowRepo
                .findByCustomerIdAndDeletedFalse(customerId);

        LedgerResponse ledger = new LedgerResponse();
        ledger.setCustomerId(customerId);
        ledger.setCustomerName(customer.getName());
        ledger.setCustomerPhone(customer.getPhone());
        ledger.setCustomerAddress(customer.getAddress());

        List<LedgerResponse.LedgerEntry> ledgerEntries =
                new ArrayList<>();

        double runningBalance = 0.0;

        // Collect all events (borrows + payments) and sort by date
        List<Object[]> events = new ArrayList<>();

        entries.forEach(b -> {
            // Add borrow event
            events.add(new Object[]{
                    b.getBorrowDate(), "BORROW",
                    b.getTotalAmount(), 0.0,
                    b.getDescription(), b.getNotes(), b.getId()
            });
            // Add each payment event
            if (b.getPayments() != null) {
                b.getPayments().stream()
                        .filter(p -> !p.isDeleted())
                        .forEach(p -> events.add(new Object[]{
                                p.getPaymentDate(), "PAYMENT",
                                0.0, p.getAmount(),
                                "Payment received", p.getNotes(), p.getId()
                        }));
            }
        });

        // Sort by date
        events.sort(Comparator.comparing(
                e -> (LocalDate) e[0]));

        for (Object[] e : events) {
            LedgerResponse.LedgerEntry le =
                    new LedgerResponse.LedgerEntry();
            le.setDate((LocalDate) e[0]);
            le.setType((String) e[1]);
            le.setAmount((Double) e[2]);
            le.setPaid((Double) e[3]);
            le.setDescription((String) e[4]);
            le.setNotes((String) e[5]);
            le.setReferenceId((Long) e[6]);

            if ("BORROW".equals(e[1])) {
                runningBalance += (Double) e[2];
            } else {
                runningBalance -= (Double) e[3];
            }
            le.setBalance(runningBalance);
            ledgerEntries.add(le);
        }

        ledger.setEntries(ledgerEntries);
        ledger.setTotalBorrowed(entries.stream()
                .mapToDouble(BorrowEntry::getTotalAmount).sum());
        ledger.setTotalPaid(entries.stream()
                .mapToDouble(BorrowEntry::getAmountPaid).sum());
        ledger.setOutstanding(runningBalance);

        return ledger;
    }

    // ── Helpers ───────────────────────────────────────────────

    private BorrowEntry getActiveOrThrow(Long id) {
        BorrowEntry entry = borrowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Borrow entry not found: " + id));
        if (entry.isDeleted())
            throw new ResourceNotFoundException(
                    "Borrow entry has been deleted");
        return entry;
    }

    private void recalculate(BorrowEntry entry) {
        double totalPaid = paymentRepo
                .findByBorrowEntryIdAndDeletedFalse(entry.getId())
                .stream().mapToDouble(BorrowPayment::getAmount).sum();
        entry.setAmountPaid(totalPaid);
        entry.setRemainingBalance(
                entry.getTotalAmount() - totalPaid);
        updateStatus(entry);
    }

    private void updateStatus(BorrowEntry entry) {
        if (entry.getRemainingBalance() <= 0) {
            entry.setStatus(BorrowStatus.CLEARED);
        } else if (entry.getAmountPaid() > 0) {
            entry.setStatus(BorrowStatus.PARTIAL);
        } else if (entry.getDueDate() != null
                && entry.getDueDate().isBefore(LocalDate.now())) {
            entry.setStatus(BorrowStatus.OVERDUE);
        } else {
            entry.setStatus(BorrowStatus.PENDING);
        }
    }

    public BorrowEntryResponse toResponse(BorrowEntry entry) {
        BorrowEntryResponse res = new BorrowEntryResponse();
        res.setId(entry.getId());
        res.setCustomerId(entry.getCustomer().getId());
        res.setCustomerName(entry.getCustomer().getName());
        res.setCustomerPhone(entry.getCustomer().getPhone());
        res.setCustomerAddress(entry.getCustomer().getAddress());
        res.setTotalAmount(entry.getTotalAmount());
        res.setAmountPaid(entry.getAmountPaid());
        res.setRemainingBalance(entry.getRemainingBalance());
        res.setBorrowDate(entry.getBorrowDate());
        res.setDueDate(entry.getDueDate());
        res.setDescription(entry.getDescription());
        res.setNotes(entry.getNotes());
        res.setTags(entry.getTags());
        res.setCreditLimit(entry.getCreditLimit());
        res.setStatus(entry.getStatus().name());
        res.setDeleted(entry.isDeleted());
        res.setCreatedAt(entry.getCreatedAt());
        res.setUpdatedAt(entry.getUpdatedAt());
        res.setOverdue(
                entry.getDueDate() != null
                        && entry.getDueDate().isBefore(LocalDate.now())
                        && entry.getStatus() != BorrowStatus.CLEARED
                        && entry.getStatus() != BorrowStatus.WRITTEN_OFF);

        if (entry.getPayments() != null) {
            res.setPayments(entry.getPayments().stream()
                    .filter(p -> !p.isDeleted())
                    .map(this::toPaymentResponse)
                    .collect(Collectors.toList()));
        }
        return res;
    }

    private BorrowPaymentResponse toPaymentResponse(
            BorrowPayment p) {
        BorrowPaymentResponse r = new BorrowPaymentResponse();
        r.setId(p.getId());
        r.setAmount(p.getAmount());
        r.setPaymentDate(p.getPaymentDate());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setNotes(p.getNotes());
        r.setDeleted(p.isDeleted());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
