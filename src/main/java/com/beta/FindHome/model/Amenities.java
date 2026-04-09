package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.Furnish;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Amenities extends BaseEntity {

    private boolean hasParking;
    private boolean hasWifi;
    private boolean hasSecurityStaff;
    private boolean hasUnderGroundWaterTank;
    private boolean hasTV;
    private boolean hasCCTV;
    private boolean hasAC;
    private boolean hasFridge;
    private boolean hasBalcony;
    private boolean hasWater;
    private boolean hasSolarWaterHeater;
    private boolean hasFan;

    @Enumerated(EnumType.STRING)
    private Furnish furnishingStatus;

    // ONE reference. Three constructors gone.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_amenities_property"))
    private Property property;
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.Furnish;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "amenities")
//@Getter
//@Setter
//@NoArgsConstructor
//@Builder
//@AllArgsConstructor
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Amenities.class
//)
//public class Amenities extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Column(nullable = true)
//    private boolean hasParking;
//
//    @Column(nullable = true)
//    private boolean hasWifi;
//
//    @Column(nullable = true)
//    private boolean hasSecurityStaff;
//
//    @Column(nullable = true)
//    private boolean hasUnderGroundWaterTank;
//
//    @Column(nullable = true)
//    private boolean hasTV;
//
//    @Column(nullable = true)
//    private boolean hasCCTV;
//
//    @Column(nullable = true)
//    private boolean hasAC;
//
//    @Column(nullable = true)
//    private boolean hasFridge;
//
//    @Column(nullable = true)
//    private boolean hasBalcony;
//
//    @Column(nullable = true)
//    private boolean hasWater;
//
//    @Column(nullable = false)
//    private boolean hasSolarWaterHeater;
//
//    @Column(nullable = true)
//    private boolean hasFan;
//
//    @Column(nullable = true)
//    private Furnish furnishingStatus;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "house_id", nullable = true, foreignKey = @ForeignKey(name = "fk_amenities_house"))
//    @JsonBackReference // Means when I fetch amenities, I don't want to fetch house
//    private House house;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "room_id", nullable = true, foreignKey = @ForeignKey(name = "fk_amenities_room"))
//    @JsonBackReference // Means when I fetch amenities, I don't want to fetch house
//    private Room room;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "flat_id", nullable = true, foreignKey = @ForeignKey(name = "fk_amenities_flat"))
//    @JsonBackReference // Means when I fetch amenities, I don't want to fetch house
//    private Flat flat;
//
//    public Amenities(House house, boolean hasParking, boolean hasWifi, boolean hasSecurityStaff, boolean hasUnderGroundWaterTank, boolean hasTV,
//                     boolean hasCCTV, boolean hasAC, boolean hasFridge, boolean hasBalcony, boolean hasWater, boolean hasSolarWaterHeater,
//                     boolean hasFan, Furnish furnishingStatus) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.house = house;
//        this.flat = null;
//        this.room = null;
//        this.hasParking = hasParking;
//        this.hasWifi = hasWifi;
//        this.hasSecurityStaff = hasSecurityStaff;
//        this.hasUnderGroundWaterTank = hasUnderGroundWaterTank;
//        this.hasTV = hasTV;
//        this.hasCCTV = hasCCTV;
//        this.hasAC = hasAC;
//        this.hasFridge = hasFridge;
//        this.hasBalcony = hasBalcony;
//        this.hasWater = hasWater;
//        this.hasSolarWaterHeater = hasSolarWaterHeater;
//        this.hasFan = hasFan;
//        this.furnishingStatus = furnishingStatus;
//    }
//
//    public Amenities(Flat flat, boolean hasParking, boolean hasWifi, boolean hasSecurityStaff, boolean hasUnderGroundWaterTank, boolean hasTV,
//                     boolean hasCCTV, boolean hasAC, boolean hasFridge, boolean hasBalcony, boolean hasWater, boolean hasSolarWaterHeater,
//                     boolean hasFan, Furnish furnishingStatus) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.flat = flat;
//        this.house = null;
//        this.room = null;
//        this.hasParking = hasParking;
//        this.hasWifi = hasWifi;
//        this.hasSecurityStaff = hasSecurityStaff;
//        this.hasUnderGroundWaterTank = hasUnderGroundWaterTank;
//        this.hasTV = hasTV;
//        this.hasCCTV = hasCCTV;
//        this.hasAC = hasAC;
//        this.hasFridge = hasFridge;
//        this.hasBalcony = hasBalcony;
//        this.hasWater = hasWater;
//        this.hasSolarWaterHeater = hasSolarWaterHeater;
//        this.hasFan = hasFan;
//        this.furnishingStatus = furnishingStatus;
//    }
//
//    public Amenities(Room room, boolean hasParking, boolean hasWifi, boolean hasSecurityStaff, boolean hasUnderGroundWaterTank, boolean hasTV,
//                     boolean hasCCTV, boolean hasAC, boolean hasFridge, boolean hasBalcony, boolean hasWater, boolean hasSolarWaterHeater,
//                     boolean hasFan) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.room = room;
//        this.house = null;
//        this.flat = null;
//        this.hasParking = hasParking;
//        this.hasWifi = hasWifi;
//        this.hasSecurityStaff = hasSecurityStaff;
//        this.hasUnderGroundWaterTank = hasUnderGroundWaterTank;
//        this.hasTV = hasTV;
//        this.hasCCTV = hasCCTV;
//        this.hasAC = hasAC;
//        this.hasFridge = hasFridge;
//        this.hasBalcony = hasBalcony;
//        this.hasWater = hasWater;
//        this.hasSolarWaterHeater = hasSolarWaterHeater;
//        this.hasFan = hasFan;
//    }
//
//}
