package com.beta.FindHome.fixtures;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.RoleStatus;
import com.beta.FindHome.model.Role;
import com.beta.FindHome.model.Users;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

    public class UsersFixture {

        public static List<Users> getPredefinedUsers() {

            Set<Role> predefinedRoles = RolesFixture.getPredefinedRoles();

            Role adminRole = findRoleByName(predefinedRoles, "ADMIN");
            Role userRole = findRoleByName(predefinedRoles, "USER");
            Role ownerRole = findRoleByName(predefinedRoles, "OWNER");

            return List.of(

                    Users.builder()
                            .firstName("John")
                            .middleName("A.")
                            .lastName("Doe")
                            .userName("johndoe")
                            .email("johndoe@example.com")
                            .password("password123")
                            .phoneNumber("1234567890")
                            .dob(LocalDate.of(1990, 1, 1))
                            .userGender(Gender.MALE)
                            .maritalStatus(MaritalStatus.SINGLE)
                            .roleStatus(RoleStatus.EMPLOYED)
                            .isVerified(true)
                            .roles(Set.of(adminRole, userRole))
                            .build(),

                    Users.builder()
                            .firstName("Jane")
                            .middleName("B.")
                            .lastName("Smith")
                            .userName("janesmith")
                            .email("janesmith@example.com")
                            .password("password123")
                            .phoneNumber("0987654321")
                            .dob(LocalDate.of(1995, 5, 15))
                            .userGender(Gender.FEMALE)
                            .maritalStatus(MaritalStatus.MARRIED)
                            .roleStatus(RoleStatus.STUDENT)
                            .isVerified(true)
                            .roles(Set.of(userRole))
                            .build(),

                    Users.builder()
                            .firstName("Carlos")
                            .middleName("C.")
                            .lastName("Gonzalez")
                            .userName("carlosg")
                            .email("carlosg@example.com")
                            .password("securePass")
                            .phoneNumber("1112223333")
                            .dob(LocalDate.of(1988, 3, 22))
                            .userGender(Gender.MALE)
                            .maritalStatus(MaritalStatus.SINGLE)
                            .roleStatus(RoleStatus.STUDENT)
                            .isVerified(true)
                            .roles(Set.of(ownerRole))
                            .build(),

                    Users.builder()
                            .firstName("Aisha")
                            .middleName("D.")
                            .lastName("Khan")
                            .userName("aishakhan")
                            .email("aisha@example.com")
                            .password("strongPass")
                            .phoneNumber("2223334444")
                            .dob(LocalDate.of(1992, 7, 10))
                            .userGender(Gender.FEMALE)
                            .maritalStatus(MaritalStatus.MARRIED)
                            .roleStatus(RoleStatus.STUDENT)
                            .isVerified(false)
                            .roles(Set.of(userRole))
                            .build(),

                    Users.builder()
                            .firstName("Liam")
                            .middleName("E.")
                            .lastName("O'Connor")
                            .userName("liamoc")
                            .email("liam@example.com")
                            .password("liamPass")
                            .phoneNumber("3334445555")
                            .dob(LocalDate.of(1997, 11, 5))
                            .userGender(Gender.MALE)
                            .maritalStatus(MaritalStatus.SINGLE)
                            .roleStatus(RoleStatus.STUDENT)
                            .isVerified(true)
                            .roles(Set.of(adminRole, ownerRole))
                            .build(),

                    Users.builder()
                            .firstName("Sophia")
                            .middleName("F.")
                            .lastName("Martinez")
                            .userName("sophiam")
                            .email("sophia@example.com")
                            .password("sophiaPass")
                            .phoneNumber("4445556666")
                            .dob(LocalDate.of(1985, 9, 30))
                            .userGender(Gender.FEMALE)
                            .maritalStatus(MaritalStatus.SINGLE)
                            .roleStatus(RoleStatus.OTHER)
                            .isVerified(true)
                            .roles(Set.of(adminRole))
                            .build(),

                    Users.builder()
                            .firstName("Nathan")
                            .middleName("G.")
                            .lastName("Brown")
                            .userName("nathanb")
                            .email("nathan@example.com")
                            .password("nathanPass")
                            .phoneNumber("5556667777")
                            .dob(LocalDate.of(2000, 6, 18))
                            .userGender(Gender.MALE)
                            .maritalStatus(MaritalStatus.SINGLE)
                            .roleStatus(RoleStatus.STUDENT)
                            .isVerified(false)
                            .roles(Set.of(userRole, ownerRole))
                            .build()
            );
        }

        private static Role findRoleByName(Set<Role> roles, String roleName) {
            return roles.stream()
                    .filter(role -> role.getRoleName().equals(roleName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        }

}