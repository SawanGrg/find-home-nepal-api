package com.beta.FindHome.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    // LAZY explicitly set — never load users when you just need a role name
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Users> users;

    public Role(String roleName) {
        this.roleName = roleName;
        // No manual timestamp — Spring Auditing handles it
    }
}

//package com.beta.FindHome.model;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.Set;
//
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//
//@Entity
//@Table(name = "role")
//@Getter
//@Setter
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id",
//        scope = Role.class
//)
//@NoArgsConstructor
//public class Role extends BaseEntity implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    @Column(name = "role_name", nullable = false, unique = true, length = 50)
//    private String roleName;
//
//    @ManyToMany(mappedBy = "roles")
//    @JsonIgnore
//    private Set<Users> users;
//
//    public Role(String roleName) {
//        this.roleName = roleName;
//        super.setCreatedAt(LocalDateTime.now());
//        super.setUpdatedAt(LocalDateTime.now());
//    }
//}
