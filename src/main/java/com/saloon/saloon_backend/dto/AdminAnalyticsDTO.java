package com.saloon.saloon_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminAnalyticsDTO {
    private RevenueStats revenueStats;
    private List<StylistPerformance> topStylists;
    private List<ServicePerformance> servicePerformance;
    private ClientInsights clientInsights;
    private List<DailyRevenue> revenueTrend; // ✅ FIXED: removed space

    @Data
    public static class RevenueStats {
        private BigDecimal todayRevenue;
        private BigDecimal weekRevenue;
        private BigDecimal monthRevenue;
        private BigDecimal avgPerClient;
        private String todayChange;
        private String weekChange;
        private String monthChange;
        private String avgChange;
    }

    @Data
    public static class StylistPerformance {
        private String name;
        private BigDecimal revenue;
        private Integer appointments;
        private BigDecimal rating;
    }

    @Data
    public static class ServicePerformance {
        private String serviceName;
        private Integer count;
        private BigDecimal revenue;
    }

    @Data
    public static class ClientInsights {
        private Integer totalClients;
        private Integer newThisMonth;
        private Integer vipClients;
    }

    @Data
    public static class DailyRevenue {
        private String date;
        private BigDecimal revenue;
        private Integer appointments;
    }
}