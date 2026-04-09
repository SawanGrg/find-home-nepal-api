package com.beta.FindHome.repository;

import com.beta.FindHome.model.Role;
import com.beta.FindHome.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Single method — findByRoleName is the correct Spring Data naming
    // Used everywhere: roleRepository.findByRoleName("USER")
    Optional<Role> findByRoleName(String roleName);

    // Used in authenticateUser() and loadUserByUsername()
    // Fine for single-user auth — do NOT call this in a loop
    Set<Role> findRolesByUsers(Users user);
}