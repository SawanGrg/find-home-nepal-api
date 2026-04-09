package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
public class Assets extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String assetType;

    @Column(nullable = false, length = 255)
    private String assetURL;

    // ONE reference replacing three nullable FKs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_assets_property"))
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
//import lombok.ToString;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//
//@Entity
//@Table(name = "assets")
//@Getter
//@Setter
//@NoArgsConstructor
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Assets.class
//)
//@ToString(callSuper = true, exclude = {"house", "room", "flat"})
//public class Assets extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Column(nullable = false, length = 50)
//    private String assetType;
//
//    @Column(nullable = false, length = 255)
//    private String assetURL;
//
//    @ManyToOne
//    @JoinColumn(name = "house_id", nullable = true)
//    @JsonBackReference
//    private House house;
//
//    @ManyToOne
//    @JoinColumn(name = "room_id", nullable = true)
//    @JsonBackReference
//    private Room room;
//
//    @ManyToOne
//    @JoinColumn(name = "flat_id", nullable = true)
//    @JsonBackReference
//    private Flat flat;
//
//    public Assets(House house, String assetType, String url) {
//        super.setCreatedAt(LocalDateTime.now());
//        this.house = house;
//        this.assetType = assetType;
//        this.assetURL = url;
//        this.room = null;
//        this.flat = null;
//    }
//
//    public Assets(Room room, String assetType, String url) {
//        super.setCreatedAt(LocalDateTime.now());
//        this.room = room;
//        this.assetType = assetType;
//        this.assetURL = url;
//        this.house = null;
//        this.flat = null;
//    }
//
//    public Assets(Flat flat, String assetType, String url) {
//        super.setCreatedAt(LocalDateTime.now());
//        this.flat = flat;
//        this.assetType = assetType;
//        this.assetURL = url;
//        this.house = null;
//        this.room = null;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Assets)) return false;
//        Assets assets = (Assets) o;
//        return getAssetType().equals(assets.getAssetType()) && getAssetURL().equals(assets.getAssetURL());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getAssetType(), getAssetURL());
//    }
//}
