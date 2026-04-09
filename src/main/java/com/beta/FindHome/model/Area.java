
package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
public class Area extends BaseEntity {

    @Column(nullable = true)
    private Float length;

    @Column(nullable = true)
    private Float breadth;

    @Column(nullable = true)
    private Float totalArea;

    // ONE reference. Works for House, Flat, Room, and any future type.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_area_property"))
    private Property property;
}

//package com.beta.FindHome.model;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "area")
//@Getter
//@Setter
//@NoArgsConstructor
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Area.class
//)
//public class Area extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Column(nullable = true)
//    private Float length;
//
//    @Column(nullable = true)
//    private Float breadth;
//
//    @Column(nullable = true)
//    private Float totalArea;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//    @JoinColumn(name = "house_id", referencedColumnName = "id", nullable = true)
//    @JsonBackReference
//    private House house;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = true)
//    @JsonBackReference
//    private Room room;
//
//    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//    @JoinColumn(name = "flat_id", referencedColumnName = "id", nullable = true)
//    @JsonBackReference
//    private Flat flat;
//
//    public Area(Float length, Float breadth) {
//        this.length = length;
//        this.breadth = breadth;
//    }
//
//    public Area(House house, Float length, Float breadth) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.house = house;
//        this.flat = null;
//        this.room = null;
//        this.length = length;
//        this.breadth = breadth;
//        this.totalArea = calculateArea();
//    }
//
//    public Area(Room room, Float length, Float breadth) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.room = room;
//        this.flat = null;
//        this.house = null;
//        this.length = length;
//        this.breadth = breadth;
//        this.totalArea = calculateArea();
//    }
//
//    public Area(Flat flat, Float length, Float breadth) {
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//        this.flat = flat;
//        this.house = null;
//        this.room = null;
//        this.length = length;
//        this.breadth = breadth;
//        this.totalArea = calculateArea();
//    }
//
//    private Float calculateArea() {
//        if (length != null && breadth != null) {
//            return length * breadth;
//        } else {
//            return null;
//        }
//    }
//}
