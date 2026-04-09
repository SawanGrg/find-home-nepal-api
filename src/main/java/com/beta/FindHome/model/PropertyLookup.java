package com.beta.FindHome.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor // Only no-args constructor
@Builder
@AllArgsConstructor
public class PropertyLookup extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "property_id", nullable = false, unique = true)
    private UUID propertyId;

    @Column(name = "table_name", nullable = false)
    private String tableName;
}