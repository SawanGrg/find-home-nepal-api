package com.beta.FindHome.repository;

import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>,
        JpaSpecificationExecutor<Property> {

    // Single query — fetches property + amenities + assets + area
    // Used by PropertyServiceImpl.getProperty() and House/Room/FlatService.getById()
    @Query("""
            SELECT p FROM Property p
            LEFT JOIN FETCH p.amenities
            LEFT JOIN FETCH p.assets
            LEFT JOIN FETCH p.area
            WHERE p.id = :id AND p.isDeleted = false
            """)
    Optional<Property> findByIdWithDetails(@Param("id") UUID id);

    // Used by PropertyServiceImpl.findLandLordIdByPropertyId()
    // LAZY landlord is fine here — caller only accesses .getLandlord()
    // which is within the same transaction
    @Query("""
            SELECT p FROM Property p
            JOIN FETCH p.landlord
            WHERE p.id = :id AND p.isDeleted = false
            """)
    Optional<Property> findByIdWithLandlord(@Param("id") UUID id);

    // Used for landlord's own property listing page
    @Query("""
            SELECT p FROM Property p
            WHERE p.landlord.id = :landlordId
            AND p.isDeleted = false
            """)
    List<Property> findAllByLandlordId(@Param("landlordId") UUID landlordId);

    // Paginated browse — available and verified only
    @Query("""
            SELECT p FROM Property p
            WHERE p.isDeleted = false
            AND p.isAvailable = true
            AND p.isVerified = true
            """)
    Page<Property> findAllAvailable(Pageable pageable);

    // Soft delete — toggles isDeleted flag without removing the row
    @Modifying
    @Transactional
    @Query("UPDATE Property p SET p.isDeleted = :isDeleted WHERE p.id = :id")
    void updateIsDeleted(@Param("id") UUID id, @Param("isDeleted") boolean isDeleted);

    // Used by admin to see all properties including deleted
    List<Property> findAllByIsDeletedFalse();

    // Safe lookup with built-in exception — avoids repetitive orElseThrow() in services
    default Property findActiveByIdOrThrow(UUID id, String entityName) {
        return findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityName + " not found with id: " + id));
    }
}