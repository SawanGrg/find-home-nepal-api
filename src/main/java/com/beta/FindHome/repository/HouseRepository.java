package com.beta.FindHome.repository;

import com.beta.FindHome.model.House;
import com.beta.FindHome.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseRepository extends JpaRepository<House, UUID>, JpaSpecificationExecutor<House> {

    // Replaces getHouseDetailsById() — one query instead of 3
    @Query("""
            SELECT h FROM House h
            LEFT JOIN FETCH h.amenities
            LEFT JOIN FETCH h.assets
            LEFT JOIN FETCH h.area
            WHERE h.id = :id AND h.isDeleted = false
            """)
    Optional<House> findActiveWithDetailsById(@Param("id") UUID id);

    boolean existsByIdAndLandlord(UUID houseId, Users landlord);

    @Query("""
            SELECT COUNT(h) > 0 FROM House h
            WHERE h.id = :houseId AND h.landlord.userName = :username
            AND h.isDeleted = false
            """)
    boolean existsActiveByIdAndLandlordUsername(@Param("houseId") UUID houseId,
                                                @Param("username") String username);
}
//package com.beta.FindHome.repository;
//
//import com.beta.FindHome.model.House;
//
//import com.beta.FindHome.model.Users;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository("houseRepository")
//public interface HouseRepository extends JpaRepository<House, UUID>, JpaSpecificationExecutor<House>{
//
//    @Query("SELECT h FROM House h LEFT JOIN FETCH h.assets LEFT JOIN FETCH h.amenities WHERE h.id = :id")
//    Optional<House> findByIdWithAssetsAndAmenities(UUID id);
//
//    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
//            "FROM House h WHERE h.id = :houseId AND h.landlordId.userName = :username")
//    boolean existsByIdAndLandlord_Username(@Param("houseId") UUID houseId,
//                                           @Param("username") String username);
//
//    boolean existsByIdAndLandlordId(UUID houseId, Users landlord);
//}
