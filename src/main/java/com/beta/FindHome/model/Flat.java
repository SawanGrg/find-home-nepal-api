package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flat")
@Getter
@Setter
@NoArgsConstructor
public class Flat extends Property {

    @Column(name = "bed_rooms")
    private Integer bedRooms;

    @Column(name = "bath_rooms")
    private Integer bathRooms;

    @Column(name = "kitchen")
    private Integer kitchen;

    @Column(name = "living_room")
    private Integer livingRoom;
}

//package com.beta.FindHome.model;
//
//
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.util.List;
//
//@Entity
//@Table(name = "flat")
//@Setter
//@Getter
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Flat.class
//)
//public class Flat extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "landlord_id", nullable = false, foreignKey = @ForeignKey(name = "fk_house_landlord"))
//    private Users landlordId;
//
//    @Column(name = "flat_price", nullable = false, precision = 10, scale = 2)
//    private BigDecimal flatPrice;
//
//    @Column(name = "flat_description", nullable = false, columnDefinition = "TEXT")
//    private String flatDescription;
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
//    @Column(name = "flat_rules", nullable = false, length = 2000)
//    private String flatRules;
//
//    @Column(name = "bed_rooms", nullable = true)
//    private Integer bedRooms;
//
//    @Column(name = "bath_rooms", nullable = true)
//    private Integer bathRooms;
//
//    @Column(name = "kitchen", nullable = true)
//    private Integer kitchen;
//
//    @Column(name = "living_room", nullable = true)
//    private Integer livingRoom;
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
//    @OneToMany(mappedBy = "flat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private List<Assets> assets;
//
//    @OneToOne(mappedBy = "flat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private Amenities amenities;
//
//    @OneToOne(mappedBy = "flat", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
//    @JsonManagedReference // WHEN I FETCH HOUSE, I WANT TO FETCH ASSETS
//    private Area area;
//
//    //Constructor
//    public Flat() {
//    }
//
//    //Constructor
//    public Flat(Users landlordId,
//                BigDecimal flatPrice,
//                String flatDescription,
//                String district,
//                String city,
//                String ward,
//                String tole,
//                String flatRules,
//                Integer bedRooms,
//                Integer bathRooms,
//                Integer kitchen,
//                Integer livingRoom,
//                Boolean isAvailable,
//                Boolean isVerified
//    ) {
//        this.landlordId = landlordId;
//        this.flatPrice = flatPrice;
//        this.flatDescription = flatDescription;
//        this.district = district;
//        this.city = city;
//        this.ward = ward;
//        this.tole = tole;
//        this.flatRules = flatRules;
//        this.bedRooms = bedRooms;
//        this.bathRooms = bathRooms;
//        this.kitchen = kitchen;
//        this.livingRoom = livingRoom;
//        this.isAvailable = isAvailable;
//        this.isVerified = isVerified;
//    }
//}
