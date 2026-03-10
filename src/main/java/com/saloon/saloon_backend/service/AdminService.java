package com.saloon.saloon_backend.service;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.entity.*;
import com.saloon.saloon_backend.entity.enums.UserRole;
import com.saloon.saloon_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final StylistProfileRepository stylistProfileRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final BookingAnalyticsService analyticsService;


    private static final ZoneId SALON_TIMEZONE = ZoneId.of("Asia/Colombo");

    public AdminService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            StylistProfileRepository stylistProfileRepository,
            SalonServiceRepository salonServiceRepository,
            DailyStatsRepository dailyStatsRepository,
            BookingAnalyticsService analyticsService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.stylistProfileRepository = stylistProfileRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.analyticsService = analyticsService;
    }

    // ✅ REMOVED - This method is not needed, use userRepository.countNewClientsThisMonth() directly
    // Lines 43-51 DELETED

    // ==================== DASHBOARD STATS ====================

    public AdminDashboardStatsDTO getDashboardStats() {
        // Total appointments today
        Integer totalAppointments = appointmentRepository.countTodayAppointments();

        // Active stylists
        Integer activeStylists = Math.toIntExact(stylistProfileRepository.findAvailableStylists().size());

        // Total clients
        Integer totalClients = userRepository.countByRole(UserRole.CLIENT);

        // Today's revenue
        BigDecimal todayRevenue = appointmentRepository.getTodayRevenue();
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        // Calculate changes (compared to yesterday)
        Integer yesterdayAppointments = getYesterdayAppointmentsCount();
        String appointmentChange = calculatePercentageChange(yesterdayAppointments, totalAppointments);

        return new AdminDashboardStatsDTO(
                totalAppointments,
                activeStylists,
                totalClients,
                todayRevenue,
                appointmentChange,
                "+0", // Stylist change (can be calculated)
                "+0", // Client change (can be calculated)
                "+0"  // Revenue change (can be calculated)
        );
    }

    private Integer getYesterdayAppointmentsCount() {
        OffsetDateTime yesterdayStart = LocalDate.now(SALON_TIMEZONE).minusDays(1)
                .atStartOfDay(SALON_TIMEZONE).toOffsetDateTime();
        OffsetDateTime yesterdayEnd = yesterdayStart.plusDays(1);

        return appointmentRepository.findByDateRange(yesterdayStart, yesterdayEnd).size();
    }

    private String calculatePercentageChange(Integer oldValue, Integer newValue) {
        if (oldValue == 0) return "+100%";
        double change = ((newValue - oldValue) * 100.0) / oldValue;
        return String.format("%+.0f%%", change);
    }

    // ==================== APPOINTMENTS ====================

    public List<AdminAppointmentDTO> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAllOrderByStartTsDesc();
        return appointments.stream()
                .map(this::mapToAdminAppointmentDTO)
                .collect(Collectors.toList());
    }

    public List<AdminAppointmentDTO> getTodayAppointments() {
        List<Appointment> appointments = appointmentRepository.findTodayAppointments();
        return appointments.stream()
                .map(this::mapToAdminAppointmentDTO)
                .collect(Collectors.toList());
    }

    public AppointmentStatsDTO getAppointmentStats() {
        Integer todayTotal = appointmentRepository.countTodayAppointments();
        Integer confirmed = appointmentRepository.countTodayAppointmentsByStatus("CONFIRMED");
        Integer pending = appointmentRepository.countTodayAppointmentsByStatus("BOOKED");
        Integer cancelled = appointmentRepository.countTodayAppointmentsByStatus("CANCELLED");

        return new AppointmentStatsDTO(todayTotal, confirmed, pending, cancelled);
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }

    private AdminAppointmentDTO mapToAdminAppointmentDTO(Appointment appointment) {
        AdminAppointmentDTO dto = new AdminAppointmentDTO();
        dto.setId(appointment.getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setClientName(appointment.getClient().getFullName());
        dto.setClientEmail(appointment.getClient().getEmail());
        dto.setClientPhone(appointment.getClient().getPhone());
        dto.setStylistId(appointment.getStylist().getId());
        dto.setStylistName(appointment.getStylist().getFullName());
        dto.setStartTs(appointment.getStartTs());
        dto.setEndTs(appointment.getEndTs());
        dto.setStatus(appointment.getStatus());
        dto.setTotalPrice(appointment.getTotalPrice());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());

        // Get service names
        List<String> serviceNames = appointment.getItems().stream()
                .map(item -> item.getService().getName())
                .collect(Collectors.toList());
        dto.setServices(serviceNames);

        return dto;
    }

    // ==================== STYLISTS ====================

    public List<AdminStylistDTO> getAllStylists() {
        List<User> stylists = userRepository.findByRole(UserRole.STYLIST);

        return stylists.stream()
                .map(this::mapToAdminStylistDTO)
                .collect(Collectors.toList());
    }

    public AdminStylistDTO getStylistById(Long stylistId) {
        User stylist = userRepository.findById(stylistId)
                .orElseThrow(() -> new IllegalArgumentException("Stylist not found"));

        return mapToAdminStylistDTO(stylist);
    }

    @Transactional
    public AdminStylistDTO createStylist(StylistCreateRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash("$2a$10$X5wGQQQNX8fQ5K5K5K5K5OeN1YxZQQQQQQQQQQQQQQQQQQQQQQ"); // Default hash
        user.setRole(UserRole.STYLIST);
        user.setStatus(com.saloon.saloon_backend.entity.enums.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // Create stylist profile
        StylistProfile profile = new StylistProfile();
        profile.setUser(savedUser);
        profile.setSpecialties(request.getSpecialties());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setBio(request.getBio());
        profile.setRating(BigDecimal.ZERO);
        profile.setTotalReviews(0);
        profile.setTotalClients(0);
        profile.setTotalRevenue(BigDecimal.ZERO);
        profile.setIsAvailable(true);

        stylistProfileRepository.save(profile);

        return mapToAdminStylistDTO(savedUser);
    }

    @Transactional
    public void updateStylistAvailability(Long stylistId, Boolean isAvailable) {
        StylistProfile profile = stylistProfileRepository.findByUserId(stylistId)
                .orElseThrow(() -> new IllegalArgumentException("Stylist profile not found"));

        profile.setIsAvailable(isAvailable);
        stylistProfileRepository.save(profile);
    }

    @Transactional
    public void deleteStylist(Long stylistId) {
        userRepository.deleteById(stylistId);
    }

    private AdminStylistDTO mapToAdminStylistDTO(User stylist) {
        AdminStylistDTO dto = new AdminStylistDTO();
        dto.setId(stylist.getId());
        dto.setName(stylist.getFullName());
        dto.setEmail(stylist.getEmail());
        dto.setPhone(stylist.getPhone());
        dto.setStatus(stylist.getStatus().name());

        // Get stylist profile
        StylistProfile profile = stylistProfileRepository.findByUserId(stylist.getId())
                .orElse(null);

        if (profile != null) {
            dto.setSpecialties(profile.getSpecialties());
            dto.setExperienceYears(profile.getExperienceYears());
            dto.setBio(profile.getBio());
            dto.setRating(profile.getRating());
            dto.setTotalReviews(profile.getTotalReviews());
            dto.setTotalClients(profile.getTotalClients());
            dto.setTotalRevenue(profile.getTotalRevenue());
            dto.setIsAvailable(profile.getIsAvailable());
        }

        return dto;
    }

    // ==================== CLIENTS ====================

    public List<AdminClientDTO> getAllClients() {
        List<User> clients = userRepository.findByRole(UserRole.CLIENT);

        return clients.stream()
                .map(this::mapToAdminClientDTO)
                .collect(Collectors.toList());
    }

    public AdminClientDTO getClientById(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        return mapToAdminClientDTO(client);
    }
    @Transactional
    public AdminClientDTO createClient(ClientCreateRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash("$2a$10$DEFAULT_HASH"); // Should use password encoder
        user.setRole(UserRole.CLIENT);
        user.setStatus(com.saloon.saloon_backend.entity.enums.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToAdminClientDTO(savedUser);
    }

    @Transactional
    public void deleteClient(Long clientId) {
        userRepository.deleteById(clientId);
    }

    private AdminClientDTO mapToAdminClientDTO(User client) {
        AdminClientDTO dto = new AdminClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getFullName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setStatus(client.getStatus().name());
        dto.setCreatedAt(client.getCreatedAt());

        // Get client statistics
        List<Appointment> completedAppointments = appointmentRepository
                .findByClientIdAndStatus(client.getId(), "COMPLETED");

        dto.setTotalVisits(completedAppointments.size());

        BigDecimal totalSpent = completedAppointments.stream()
                .map(Appointment::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSpent(totalSpent);

        // Get last visit
        if (!completedAppointments.isEmpty()) {
            dto.setLastVisit(completedAppointments.get(0).getStartTs());
        }

        // Determine membership tier
        if (dto.getTotalVisits() >= 50) {
            dto.setMembershipTier("VIP");
        } else if (dto.getTotalVisits() >= 20) {
            dto.setMembershipTier("Gold");
        } else {
            dto.setMembershipTier("Regular");
        }

        return dto;
    }

    // ==================== SERVICES ====================

    public List<AdminServiceDTO> getAllServices() {
        List<SalonService> services = salonServiceRepository.findAll();

        return services.stream()
                .map(this::mapToAdminServiceDTO)
                .collect(Collectors.toList());
    }

    public AdminServiceDTO getServiceById(Long serviceId) {
        SalonService service = salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        return mapToAdminServiceDTO(service);
    }

    @Transactional
    public AdminServiceDTO createService(ServiceCreateRequest request) {
        SalonService service = new SalonService();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDefaultDurationMin(request.getDurationMin());
        service.setBasePrice(request.getPrice());
        service.setCategory(request.getCategory());
        service.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        SalonService saved = salonServiceRepository.save(service);

        return mapToAdminServiceDTO(saved);
    }

    @Transactional
    public AdminServiceDTO updateService(Long serviceId, ServiceCreateRequest request) {
        SalonService service = salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDefaultDurationMin(request.getDurationMin());
        service.setBasePrice(request.getPrice());
        service.setCategory(request.getCategory());
        service.setIsActive(request.getIsActive());

        SalonService updated = salonServiceRepository.save(service);

        return mapToAdminServiceDTO(updated);
    }

    @Transactional
    public void deleteService(Long serviceId) {
        salonServiceRepository.deleteById(serviceId);
    }

    private AdminServiceDTO mapToAdminServiceDTO(SalonService service) {
        AdminServiceDTO dto = new AdminServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setDurationMin(service.getDefaultDurationMin());
        dto.setPrice(service.getBasePrice());
        dto.setCategory(service.getCategory());
        dto.setIsActive(service.getIsActive());

        // Set total bookings and revenue (default to 0 if not available)
        dto.setTotalBookings(0);
        dto.setTotalRevenue(BigDecimal.ZERO);

        return dto;
    }

    // ==================== ANALYTICS ====================

    public AdminAnalyticsDTO getAnalytics() {
        AdminAnalyticsDTO analytics = new AdminAnalyticsDTO();

        // Revenue stats
        analytics.setRevenueStats(getRevenueStats());

        // Top stylists
        analytics.setTopStylists(getTopStylists());

        // Service performance
        analytics.setServicePerformance(getServicePerformance());

        // Client insights
        analytics.setClientInsights(getClientInsights());

        // Revenue trend (last 7 days)
        analytics.setRevenueTrend(getRevenueTrend());

        return analytics;
    }

    private AdminAnalyticsDTO.RevenueStats getRevenueStats() {
        AdminAnalyticsDTO.RevenueStats stats = new AdminAnalyticsDTO.RevenueStats();

        OffsetDateTime now = OffsetDateTime.now(SALON_TIMEZONE);
        OffsetDateTime todayStart = now.toLocalDate().atStartOfDay(SALON_TIMEZONE).toOffsetDateTime();
        OffsetDateTime todayEnd = todayStart.plusDays(1);

        OffsetDateTime weekStart = todayStart.minusDays(7);
        OffsetDateTime monthStart = todayStart.minusDays(30);

        // Today
        BigDecimal todayRevenue = appointmentRepository.getRevenueByDateRange(todayStart, todayEnd);
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // This week
        BigDecimal weekRevenue = appointmentRepository.getRevenueByDateRange(weekStart, todayEnd);
        stats.setWeekRevenue(weekRevenue != null ? weekRevenue : BigDecimal.ZERO);

        // This month
        BigDecimal monthRevenue = appointmentRepository.getRevenueByDateRange(monthStart, todayEnd);
        stats.setMonthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO);

        // Average per client
        Integer totalClients = userRepository.countByRole(UserRole.CLIENT);
        if (totalClients > 0 && monthRevenue != null) {
            stats.setAvgPerClient(monthRevenue.divide(BigDecimal.valueOf(totalClients), 2, RoundingMode.HALF_UP));
        } else {
            stats.setAvgPerClient(BigDecimal.ZERO);
        }

        // Changes (placeholders)
        stats.setTodayChange("+18%");
        stats.setWeekChange("+12%");
        stats.setMonthChange("+8%");
        stats.setAvgChange("+5%");

        return stats;
    }

    private List<AdminAnalyticsDTO.StylistPerformance> getTopStylists() {
        List<StylistProfile> profiles = stylistProfileRepository.findAll();

        return profiles.stream()
                .sorted((a, b) -> {
                    BigDecimal revenueA = a.getTotalRevenue() != null ? a.getTotalRevenue() : BigDecimal.ZERO;
                    BigDecimal revenueB = b.getTotalRevenue() != null ? b.getTotalRevenue() : BigDecimal.ZERO;
                    return revenueB.compareTo(revenueA);
                })
                .limit(5)
                .map(profile -> {
                    AdminAnalyticsDTO.StylistPerformance perf = new AdminAnalyticsDTO.StylistPerformance();
                    perf.setName(profile.getUser().getFullName());
                    perf.setRevenue(profile.getTotalRevenue() != null ? profile.getTotalRevenue() : BigDecimal.ZERO);
                    perf.setAppointments(profile.getTotalClients() != null ? profile.getTotalClients() : 0);
                    perf.setRating(profile.getRating() != null ? profile.getRating() : BigDecimal.ZERO);
                    return perf;
                })
                .collect(Collectors.toList());
    }

    private List<AdminAnalyticsDTO.ServicePerformance> getServicePerformance() {
        List<SalonService> services = salonServiceRepository.findAll();

        return services.stream()
                .limit(5)
                .map(service -> {
                    AdminAnalyticsDTO.ServicePerformance perf = new AdminAnalyticsDTO.ServicePerformance();
                    perf.setServiceName(service.getName());
                    perf.setCount(0); // Would need to query appointment_items
                    perf.setRevenue(BigDecimal.ZERO); // Would need to query appointment_items
                    return perf;
                })
                .collect(Collectors.toList());
    }

    private AdminAnalyticsDTO.ClientInsights getClientInsights() {
        AdminAnalyticsDTO.ClientInsights insights = new AdminAnalyticsDTO.ClientInsights();

        Integer totalClients = userRepository.countByRole(UserRole.CLIENT);
        insights.setTotalClients(totalClients);

        // ✅ FIXED: Use the correct method from UserRepository
        Integer newThisMonth = userRepository.countNewClientsThisMonth();
        insights.setNewThisMonth(newThisMonth);

        // VIP clients (50+ visits)
        List<User> clients = userRepository.findByRole(UserRole.CLIENT);
        long vipCount = clients.stream()
                .filter(client -> {
                    int visits = appointmentRepository.findByClientIdAndStatus(client.getId(), "COMPLETED").size();
                    return visits >= 50;
                })
                .count();
        insights.setVipClients((int) vipCount);

        return insights;
    }

    private List<AdminAnalyticsDTO.DailyRevenue> getRevenueTrend() {
        LocalDate sevenDaysAgo = LocalDate.now(SALON_TIMEZONE).minusDays(7);
        List<DailyStats> stats = dailyStatsRepository.findByStatDateAfterOrderByStatDateDesc(sevenDaysAgo);

        return stats.stream()
                .map(stat -> {
                    AdminAnalyticsDTO.DailyRevenue revenue = new AdminAnalyticsDTO.DailyRevenue();
                    revenue.setDate(stat.getStatDate().format(DateTimeFormatter.ofPattern("MMM dd")));
                    revenue.setRevenue(stat.getTotalRevenue() != null ? stat.getTotalRevenue() : BigDecimal.ZERO);
                    revenue.setAppointments(stat.getTotalAppointments() != null ? stat.getTotalAppointments() : 0);
                    return revenue;
                })
                .collect(Collectors.toList());
    }
    public Map<String, Object> getPredictiveAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        // Get weekly peak hours
        Map<String, List<PeakHourDTO>> weeklyPeaks = analyticsService.getWeeklyPeakHours();
        analytics.put("weeklyPeakHours", weeklyPeaks);

        // Get booking trends (last 30 days)
        List<BookingTrendDTO> trends = analyticsService.getBookingTrends(30);
        analytics.put("bookingTrends", trends);

        // Get top busiest slots
        List<PeakHourDTO> topSlots = analyticsService.getTopBusiestSlots();
        analytics.put("topBusiestSlots", topSlots);

        // Get today's capacity utilization
        Map<String, Object> todayUtilization = analyticsService.getTodayCapacityUtilization();
        analytics.put("todayCapacity", todayUtilization);

        return analytics;
    }

    /**
     * Get peak hours for specific day
     */
    public List<PeakHourDTO> getPeakHoursForDay(String dayName) {
        DayOfWeek day = DayOfWeek.valueOf(dayName.toUpperCase());
        return analyticsService.getPeakHoursForDay(day);
    }

    /**
     * Force analytics recalculation
     */
    public String forceAnalyticsRecalculation() {
        return analyticsService.forceRecalculation();
    }
}