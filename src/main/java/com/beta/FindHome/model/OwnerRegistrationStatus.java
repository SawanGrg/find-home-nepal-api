package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.OwnerRegistrationStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "owner_registration_status")
public class OwnerRegistrationStatus extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OwnerRegistrationStatusType status;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_owner_registration_user"))
    private Users user;
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.OwnerRegistrationStatusType;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Table(name = "owner_registration_status")
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id"
//)
//public class OwnerRegistrationStatus {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "status", nullable = false, length = 50)
//    private OwnerRegistrationStatusType status; // e.g., "PENDING", "REJECTED", "APPROVED"
//
//    @Column(name = "message", columnDefinition = "TEXT")
//    private String message; // Reason for rejection or pending status
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private Users user;
//
//
//    public OwnerRegistrationStatus(OwnerRegistrationStatusType status, String message, Users user) {
//        this.status = status;
//        this.message = message;
//        this.user = user;
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//}