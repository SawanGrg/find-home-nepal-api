package com.beta.FindHome.service.security;

import com.beta.FindHome.model.Users;
import com.beta.FindHome.model.security.UserPrincipal;
import com.beta.FindHome.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindHomeUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Single query with roles — no N+1
        Users user = userRepository.findByUserNameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        // Build UserPrincipal — roles already loaded from query above
        return UserPrincipal.from(user, user.getRoles());

        // What Spring Security stores in SecurityContext:
        // UserPrincipal { id, username, password, verified, authorities }
        // citizenship docs → never loaded
        // dob → never loaded
        // marital status → never loaded
    }
}
