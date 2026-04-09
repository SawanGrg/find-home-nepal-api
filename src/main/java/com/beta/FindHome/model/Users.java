package com.beta.FindHome.model;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.RoleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString(exclude = {"roles", "properties", "participants"})
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_name", columnList = "userName"),
        @Index(name = "idx_phone_number", columnList = "phoneNumber")
})
public class Users extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Personal data — this is all Users should know about
    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "citizenship_front")
    private String citizenshipFront;

    @Column(name = "citizenship_back")
    private String citizenshipBack;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender")
    private Gender userGender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_status")
    private RoleStatus roleStatus;

    // Roles — LAZY now, loaded only when explicitly needed
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Property ownership
    @Builder.Default
    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties = new ArrayList<>();

    // Messaging participation
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participant> participants = new HashSet<>();

    // Verification status
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private OwnerRegistrationStatus ownerRegistrationStatus;

    // Correct equals/hashCode for JPA entities
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Users users = (Users) o;
        return getId() != null && Objects.equals(getId(), users.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}

//package com.beta.FindHome.model;
//
//import com.beta.FindHome.enums.model.Gender;
//import com.beta.FindHome.enums.model.MaritalStatus;
//import com.beta.FindHome.enums.model.RoleStatus;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.proxy.HibernateProxy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDate;
//import java.util.*;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@ToString(exclude = {"roles", "properties", "participants"})
////@ToString(exclude = {"roles", "houses", "flats", "rooms"}) // Prevents lazy-loading issues
//// Prevents performance issues
//@Entity
//@Table(name = "users", indexes = {
//        @Index(name = "idx_user_name", columnList = "userName"),
//        @Index(name = "idx_phone_number", columnList = "phoneNumber"),
//})
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Users.class
//)
//public class Users extends BaseEntity implements UserDetails, Serializable {
//
//    private static final Logger logger = LoggerFactory.getLogger(Users.class);
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Column(name = "first_name", nullable = false, length = 255)
//    private String firstName;
//
//    @Column(name = "middle_name", nullable = true, length = 255)
//    private String middleName;
//
//    @Column(name = "last_name", nullable = false, length = 255)
//    private String lastName;
//
//    @Column(name = "user_name", nullable = false, length = 255)
//    private String userName;
//
//    @Column(name = "email", nullable = false, unique = true, length = 255)
//    private String email;
//
//    @Column(name = "password", length = 255)
//    private String password;
//
//    @Column(name = "phone_number", length = 15)
//    private String phoneNumber;
//
//    @Column(name = "citizenship_front", nullable = true)
//    private String citizenshipFront;
//
//    @Column(name = "citizenship_back", nullable = true)
//    private String citizenshipBack;
//
//    @Column(name = "dob", nullable = false)
//    private LocalDate dob;
//
//    @Column(name = "is_verified", nullable = false)
//    private Boolean isVerified;
//
//    @Column(name = "user_gender", nullable = true)
//    @Enumerated(EnumType.STRING)
//    private Gender userGender;
//
//    @Column(name = "marital_status", nullable = true)
//    @Enumerated(EnumType.STRING)
//    private MaritalStatus maritalStatus;
//
//    @Column(name = "role_status", nullable = true)
//    @Enumerated(EnumType.STRING)
//    private RoleStatus roleStatus;
//
//    @Builder.Default
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "user_roles",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id")
//    )
//    private Set<Role> roles = new HashSet<>();
//
//    // Remove the three separate lists entirely.
//    // Add this single relationship instead.
//    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Property> properties = new ArrayList<>();
//
////    @Builder.Default
////    @OneToMany(mappedBy = "landlordId", cascade = CascadeType.ALL, orphanRemoval = true)
////    private List<House> houses = new ArrayList<>();
////
////    @Builder.Default
////    @OneToMany(mappedBy = "landlordId", cascade = CascadeType.ALL, orphanRemoval = true)
////    private List<Flat> flats = new ArrayList<>();
////
////    @Builder.Default
////    @OneToMany(mappedBy = "landlordId", cascade = CascadeType.ALL, orphanRemoval = true)
////    private List<Room> rooms = new ArrayList<>();
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Participant> participants = new HashSet<>();
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private OwnerRegistrationStatus ownerRegistrationStatus;
//
//    public Users(String firstName, String middleName, String lastName,
//                 String userName, String email, String password,
//                 String phoneNumber, LocalDate dob,Gender userGender, MaritalStatus maritalStatus,
//                 RoleStatus roleStatus, Boolean isVerified) {
//        this.firstName = firstName;
//        this.middleName = middleName;
//        this.lastName = lastName;
//        this.userName = userName;
//        this.email = email;
//        this.password = password;
//        this.phoneNumber = phoneNumber;
//        this.dob = dob;
//        this.userGender = userGender;
//        this.maritalStatus = maritalStatus;
//        this.roleStatus = roleStatus;
//        this.isVerified = isVerified;
//    }
//
//    // UserDetails interface methods
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//        logger.debug("Roles from the user before: {}", roles);
//        for (Role userRole : roles) {
//            logger.debug("Role name from user role: {}", userRole.getRoleName());
//            authorities.add(new SimpleGrantedAuthority(userRole.getRoleName()));
//        }
//        logger.debug("Authorities from the user: {}", authorities);
//        return authorities;
//    }
//
//    @Override
//    public String getUsername() {
//        return this.userName;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return isVerified;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return isVerified;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return isVerified;
//    }
//
//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null) return false;
//        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
//        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
//        if (thisEffectiveClass != oEffectiveClass) return false;
//        Users users = (Users) o;
//        return getId() != null && Objects.equals(getId(), users.getId());
//    }
//
//    @Override
//    public final int hashCode() {
//        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
//    }
//}
//
