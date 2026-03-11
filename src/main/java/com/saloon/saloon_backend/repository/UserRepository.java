package com.saloon.saloon_backend.repository;

import com.saloon.saloon_backend.entity.User;
import com.saloon.saloon_backend.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ==================== ADMIN QUERIES ====================

    /**
     * Find all users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Integer countByRole(@Param("role") UserRole role);

    /**
     * Count new clients in the last 30 days
     * Using native query because JPQL doesn't support INTERVAL syntax well
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE role = 'CLIENT' AND created_at >= CURRENT_DATE - INTERVAL '30 days'", nativeQuery = true)
    Integer countNewClientsThisMonth();
}