package com.beta.FindHome.repository;

import com.beta.FindHome.model.OwnerRegistrationStatus;
import com.beta.FindHome.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRegistrationStatusRepository extends JpaRepository<OwnerRegistrationStatus, UUID> {

    // Replaces findByUsername() and fetchUserIsVerified()
    Optional<OwnerRegistrationStatus> findByUserUserName(String username);
    Optional<OwnerRegistrationStatus> findByUser(Users user);
}