package com.beta.FindHome.repository;

import com.beta.FindHome.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<Users, UUID> {
    // Add to UserRepository.java
    @Query("SELECT u.userName FROM Users u WHERE u.isVerified = true")
    List<String> getAllVerifiedUsernames();

    // Already existed — kept as-is
    @Query("SELECT u FROM Users u JOIN FETCH u.roles WHERE u.userName = :username")
    Optional<Users> findByUserNameWithRoles(@Param("username") String username);

    Users findByUserName(String username);
    Users findByPhoneNumber(String phoneNumber);
    Optional<Users> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM Users u JOIN u.roles r WHERE r.roleName = 'OWNER'")
    Page<Users> findAllOwners(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT u) FROM Users u JOIN u.roles r WHERE r.roleName = 'OWNER'")
    long countAllOwners();

    Page<Users> findAllByRoles_RoleName(String roleName, Pageable pageable);

    // --- Replaces getAllVerifiedUsernames() ---
    @Query("SELECT u.userName FROM Users u WHERE u.isVerified = true")
    List<String> findAllVerifiedUsernames();

    // --- Replaces findUsersByRole() ---
    @Query("SELECT DISTINCT u FROM Users u JOIN u.roles r WHERE r.roleName = :roleName")
    List<Users> findAllByRoleName(@Param("roleName") String roleName);

    // --- Replaces countUsers() ---
    // count() is already provided by JpaRepository — no need to add anything

    // --- Replaces approveOwnerRegistration() ---
    // Business logic with role validation belongs in service, not repository.
    // Only the update query lives here:
    @Modifying
    @Transactional
    @Query("UPDATE Users u SET u.isVerified = true WHERE u.id = :id")
    void verifyOwner(@Param("id") UUID id);

    // --- Replaces deleteById() in DAOImpl ---
    // deleteById() already provided by JpaRepository — no need to add anything

    // --- Used by approveOwnerRegistration() role check in service ---
    @Query("SELECT COUNT(u) > 0 FROM Users u JOIN u.roles r WHERE u.id = :id AND r.roleName = :roleName")
    boolean hasRole(@Param("id") UUID id, @Param("roleName") String roleName);
}

//package com.beta.FindHome.repository;
//
//import com.beta.FindHome.model.Users;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository("userRepository")
//public interface UserRepository extends JpaRepository<Users, UUID> {
//
//    // Loads user AND roles in one query — no N+1, no lazy loading issues
//    @Query("SELECT u FROM Users u JOIN FETCH u.roles WHERE u.userName = :username")
//    Optional<Users> findByUserNameWithRoles(@Param("username") String username);
//
//    Users findByUserName(String username);
//    Users findByPhoneNumber(String phoneNumber);
//    Optional<Users> findByEmail(String email);
//    @Query("SELECT DISTINCT u FROM Users u JOIN u.roles r WHERE r.roleName = 'OWNER'")
//    Page<Users> findAllOwners(Pageable pageable);
//
//    @Query("SELECT COUNT(DISTINCT u) FROM Users u JOIN u.roles r WHERE r.roleName = 'OWNER'")
//    long countAllOwners();
//
//    Page<Users> findAllByRoles_RoleName(String roleName, Pageable pageable);
//
//}
