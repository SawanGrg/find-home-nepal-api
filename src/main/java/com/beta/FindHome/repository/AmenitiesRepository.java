package com.beta.FindHome.repository;

import com.beta.FindHome.model.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AmenitiesRepository extends JpaRepository<Amenities, UUID> {
    Optional<Amenities> findByPropertyId(UUID propertyId);
}
