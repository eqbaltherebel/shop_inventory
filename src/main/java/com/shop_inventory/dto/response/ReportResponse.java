package com.shop_inventory.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReportResponse {
    private String fromDate;
    private String toDate;

    // Summary
    private Integer totalSales;
    private Double totalRevenue;
    private Double totalCost;
    private Double totalProfit;
    private Double profitMargin;     // profit / revenue * 100

    // Breakdowns
    private Map<String, Double> revenueByDay;    // date → revenue
    private Map<String, Double> profitByDay;     // date → profit
    private Map<String, Integer> salesByDay;     // date → count
    private Map<String, Double> revenueByPayment;// method → revenue

    // Top selling items
    private List<TopItemResponse> topItems;

    // All sales in range
    private List<SaleResponse> sales;
}
