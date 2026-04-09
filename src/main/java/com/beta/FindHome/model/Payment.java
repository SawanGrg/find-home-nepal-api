package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_payment_user",   columnList = "user_id")
})
public class Payment extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_user"))
    private Users user;

    // Real FK relationship — referential integrity enforced by DB
    // If property is deleted, payment record integrity is maintained
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_property"))
    private Property property;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;  // primitive — no null state

    @PrePersist
    public void prePersist() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
        if (this.expiryDate == null) {
            this.expiryDate = this.paymentDate.plusDays(14);
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
        // isActive has no null check needed — primitive defaults to false
    }
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.PaymentStatus;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Table(name = "payments", indexes = {
//        @Index(name = "idx_payment_id", columnList = "id")
//})
//public class Payment extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_user"))
//    private Users user;
//
//    @Column(name = "property_id", nullable = false)
//    private UUID propertyId; // References property from PropertyLookup
//
//    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
//    private BigDecimal amount;
//
//    @Column(name = "payment_date", nullable = false)
//    private LocalDateTime paymentDate;
//
//    @Column(name = "expiry_date", nullable = false)
//    private LocalDateTime expiryDate; // paymentDate + 14 days
//
//    @Column(name = "payment_method", nullable = false, length = 50)
//    private String paymentMethod; // e.g., "khalti", "e-sewa", etc.
//
//    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
//    private String transactionId;
//
//    @Column(name = "payment_status", nullable = false, length = 20)
//    @Enumerated(EnumType.STRING)
//    private PaymentStatus paymentStatus; // PENDING, COMPLETED, FAILED, REFUNDED
//
//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive; // True if subscription is currently active
//
//    // PrePersist method to set default values
//    @PrePersist
//    public void prePersist() {
//        if (this.paymentDate == null) {
//            this.paymentDate = LocalDateTime.now();
//        }
//        if (this.expiryDate == null) {
//            this.expiryDate = this.paymentDate.plusDays(14);
//        }
//        if (this.isActive == null) {
//            this.isActive = false;
//        }
//        if (this.paymentStatus == null) {
//            this.paymentStatus = PaymentStatus.PENDING;
//        }
//    }
//
//}
