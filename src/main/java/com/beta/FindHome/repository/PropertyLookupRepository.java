package com.beta.FindHome.repository;

import com.beta.FindHome.model.PropertyLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyLookupRepository extends JpaRepository<PropertyLookup, UUID> {
    PropertyLookup findByPropertyId(UUID propertyId);
    PropertyLookup findByTableName(String tableName);
}
