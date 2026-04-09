// RoomRepository.java
package com.beta.FindHome.repository;

import com.beta.FindHome.model.Room;
import com.beta.FindHome.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {

    // Replaces getRoomDetailsById() — one query instead of 3 separate ones
    @Query("""
            SELECT r FROM Room r
            LEFT JOIN FETCH r.amenities
            LEFT JOIN FETCH r.assets
            LEFT JOIN FETCH r.area
            WHERE r.id = :id AND r.isDeleted = false
            """)
    Optional<Room> findActiveWithDetailsById(@Param("id") UUID id);

    boolean existsByIdAndLandlord(UUID roomId, Users landlord);

    @Query("""
            SELECT COUNT(r) > 0 FROM Room r
            WHERE r.id = :roomId AND r.landlord.userName = :username
            AND r.isDeleted = false
            """)
    boolean existsActiveByIdAndLandlordUsername(@Param("roomId") UUID roomId,
                                                @Param("username") String username);
}

//package com.beta.FindHome.repository;
//
//import com.beta.FindHome.model.Room;
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
//public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {
//
//    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
//            "FROM Room r WHERE r.id = :roomId AND r.landlordId.userName = :username")
//    boolean existsByIdAndLandlord_Username(@Param("roomId") UUID roomId,
//                                                 @Param("username") String username);
//    // Add this for better performance (Spring Data JPA will implement it)
//    boolean existsByIdAndLandlordId(UUID roomId, Users landlord);
//
//}
