package com.beta.FindHome.fixtures;

import com.beta.FindHome.model.Role;
import java.util.Set;

public class RolesFixture {

    public static Set<Role> getPredefinedRoles() {
        return Set.of(
                new Role("ADMIN"),
                new Role("USER"),
                new Role("OWNER")
        );
    }
}