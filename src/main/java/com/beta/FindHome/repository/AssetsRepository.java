package com.beta.FindHome.repository;

import com.beta.FindHome.model.Assets;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface AssetsRepository extends JpaRepository<Assets, UUID> {

    List<Assets> findAllByPropertyId(UUID propertyId);

    @Modifying
    @Transactional
    void deleteAllByPropertyId(UUID propertyId);
}