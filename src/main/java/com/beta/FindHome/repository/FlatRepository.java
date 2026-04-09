//package com.beta.FindHome.repository;
//
//import com.beta.FindHome.model.Flat;
//import com.beta.FindHome.model.Users;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.UUID;
//
//@Repository
//public interface FlatRepository extends JpaRepository<Flat, UUID>, JpaSpecificationExecutor<Flat> {
//
//    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
//            "FROM Flat f WHERE f.id = :flatId AND f.landlordId.userName = :username")
//    boolean existsByIdAndLandlord_Username(@Param("flatId") UUID flatId,
//                                           @Param("username") String username);
//
//    boolean existsByIdAndLandlordId(UUID flatId, Users landlord);
//}

// FlatRepository.java
package com.beta.FindHome.repository;

import com.beta.FindHome.model.Flat;
import com.beta.FindHome.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlatRepository extends JpaRepository<Flat, UUID>, JpaSpecificationExecutor<Flat> {

    // Replaces getFlatDetailsById() — eager fetch in one query instead of 3 separate queries
    @Query("""
            SELECT f FROM Flat f
            LEFT JOIN FETCH f.amenities
            LEFT JOIN FETCH f.assets
            LEFT JOIN FETCH f.area
            WHERE f.id = :id AND f.isDeleted = false
            """)
    Optional<Flat> findActiveWithDetailsById(@Param("id") UUID id);

    boolean existsByIdAndLandlord(UUID flatId, Users landlord);

    @Query("""
            SELECT COUNT(f) > 0 FROM Flat f
            WHERE f.id = :flatId AND f.landlord.userName = :username
            AND f.isDeleted = false
            """)
    boolean existsActiveByIdAndLandlordUsername(@Param("flatId") UUID flatId,
                                                @Param("username") String username);
}