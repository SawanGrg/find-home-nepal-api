package com.beta.FindHome.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
public abstract class Property extends BaseEntity {

    // Who owns this property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_landlord"))
    private Users landlord;

    // Price — common to all property types
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Description — common to all
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // Address — extracted into embeddable, no duplication
    @Embedded
    private Address address;

    // Rules — common to all
    @Column(name = "rules", nullable = false, length = 2000)
    private String rules;

    // Status flags — primitive boolean, NOT nullable wrapper
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // All relationships now live HERE — subtypes inherit them for free
    @OneToMany(mappedBy = "property",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private List<Assets> assets = new ArrayList<>();

    @OneToOne(mappedBy = "property",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private Amenities amenities;

    @OneToOne(mappedBy = "property",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private Area area;
}