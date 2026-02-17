package com.saloon.saloon_backend.controller;

import com.saloon.saloon_backend.dto.*;
import com.saloon.saloon_backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ==================== APPOINTMENTS ====================

    @GetMapping("/appointments")
    public ResponseEntity<List<AdminAppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(adminService.getAllAppointments());
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<AdminAppointmentDTO>> getTodayAppointments() {
        return ResponseEntity.ok(adminService.getTodayAppointments());
    }

    @GetMapping("/appointments/stats")
    public ResponseEntity<AppointmentStatsDTO> getAppointmentStats() {
        return ResponseEntity.ok(adminService.getAppointmentStats());
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<Void> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        adminService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        adminService.deleteAppointment(id);
        return ResponseEntity.ok().build();
    }

    // ==================== STYLISTS ====================

    @GetMapping("/stylists")
    public ResponseEntity<List<AdminStylistDTO>> getAllStylists() {
        return ResponseEntity.ok(adminService.getAllStylists());
    }

    @GetMapping("/stylists/{id}")
    public ResponseEntity<AdminStylistDTO> getStylistById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getStylistById(id));
    }

    @PostMapping("/stylists")
    public ResponseEntity<AdminStylistDTO> createStylist(@RequestBody StylistCreateRequest request) {
        return ResponseEntity.ok(adminService.createStylist(request));
    }

    @PutMapping("/stylists/{id}/availability")
    public ResponseEntity<Void> updateStylistAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isAvailable) {
        adminService.updateStylistAvailability(id, isAvailable);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stylists/{id}")
    public ResponseEntity<Void> deleteStylist(@PathVariable Long id) {
        adminService.deleteStylist(id);
        return ResponseEntity.ok().build();
    }

    // ==================== CLIENTS ====================

    @GetMapping("/clients")
    public ResponseEntity<List<AdminClientDTO>> getAllClients() {
        return ResponseEntity.ok(adminService.getAllClients());
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<AdminClientDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getClientById(id));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        adminService.deleteClient(id);
        return ResponseEntity.ok().build();
    }

    // ==================== SERVICES ====================

    @GetMapping("/services")
    public ResponseEntity<List<AdminServiceDTO>> getAllServices() {
        return ResponseEntity.ok(adminService.getAllServices());
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<AdminServiceDTO> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getServiceById(id));
    }

    @PostMapping("/services")
    public ResponseEntity<AdminServiceDTO> createService(@RequestBody ServiceCreateRequest request) {
        return ResponseEntity.ok(adminService.createService(request));
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<AdminServiceDTO> updateService(
            @PathVariable Long id,
            @RequestBody ServiceCreateRequest request) {
        return ResponseEntity.ok(adminService.updateService(id, request));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        adminService.deleteService(id);
        return ResponseEntity.ok().build();
    }

    // ==================== ANALYTICS ====================

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }


    @PostMapping("/clients")
    public ResponseEntity<AdminClientDTO> createClient(@RequestBody ClientCreateRequest request) {
        return ResponseEntity.ok(adminService.createClient(request));
    }
}
