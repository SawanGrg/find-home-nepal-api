package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class Room extends Property {
    // Rooms currently have no unique fields beyond what Property provides.
    // When you add room-specific fields (e.g., roomType, isShared), they go here.
}

//package com.beta.FindHome.model;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.Data;
//import org.apache.poi.ss.formula.functions.Roman;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Set;
//
//@Entity
//@Data
//@Table(name = "room")
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Roman.class
//)
//public class Room extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "landlord_id", nullable = false, foreignKey = @ForeignKey(name = "fk_house_landlord"))
//    private Users landlordId;
//
//    @Column(name = "room_price", nullable = false, precision = 10, scale = 2)
//    private BigDecimal roomPrice;
//
//    @Column(name = "room_description", nullable = false, columnDefinition = "TEXT")
//    private String roomDescription;
//
//    @Column(name = "district", nullable = false, length = 255)
//    private String district;
//
//    @Column(name = "city", nullable = false, length = 100)
//    private String city;
//
//    @Column(name = "ward", nullable = false, length = 100)
//    private String ward;
//
//    @Column(name = "tole", nullable = false, length = 100)
//    private String tole;
//
//    @Column(name = "room_rules", nullable = false, length = 2000)
//    private String roomRules;
//
//    @Column(name="is_Available", nullable = true)
//    private Boolean isAvailable;
//
//    @Column(name = "is_verified", nullable = true)
//    private Boolean isVerified;
//
//    @Column (name="is_deleted", nullable = true)
//    private Boolean isDeleted;
//
//    @OneToMany(mappedBy = "room", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private List<Assets> assets;
//
//    @OneToOne(mappedBy = "room", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private Amenities amenities;
//
//    @OneToOne(mappedBy = "room", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private Area area;
//
//    //Constructor
//    public Room(Users landlordId,
//                BigDecimal roomPrice,
//                String roomDescription,
//                String district,
//                String city,
//                String ward,
//                String tole,
//                String roomRules,
//                Boolean isAvailable,
//                Boolean isVerified
//    ) {
//        this.landlordId = landlordId;
//        this.roomPrice = roomPrice;
//        this.roomDescription = roomDescription;
//        this.district = district;
//        this.city = city;
//        this.ward = ward;
//        this.tole = tole;
//        this.roomRules = roomRules;
//        this.isAvailable = isAvailable;
//        this.isVerified = isVerified;
//    }
//
//    public Room() {
//    }
//}
