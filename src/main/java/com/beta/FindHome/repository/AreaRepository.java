package com.beta.FindHome.repository;

import com.beta.FindHome.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    Optional<Area> findByPropertyId(UUID propertyId);
}