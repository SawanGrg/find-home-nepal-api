package com.beta.FindHome.config;

import com.beta.FindHome.exception.JwtAuthException;
import com.beta.FindHome.filter.*;
import com.beta.FindHome.service.user.UserServiceImpl;
import com.beta.FindHome.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig implements WebMvcConfigurer {

    private final UserServiceImpl userServiceImplementation;
    private final PasswordConfig passwordConfig;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CorsConfig corsConfig;

    @Autowired
    public WebSecurityConfig(
            UserServiceImpl userServiceImplementation,
            PasswordConfig passwordConfig,
            JwtUtils jwtUtils,
            RedisTemplate<String, Object> redisTemplate,
            CorsConfig corsConfig
    ) {
        this.userServiceImplementation = userServiceImplementation;
        this.passwordConfig = passwordConfig;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.corsConfig = corsConfig;
    }

    private static final String[] PUBLIC_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/register",
            "/api/v1/auth/**",
            "/api/v1/auth/owner/token/verification/**",
            "/api/v1/public/all-properties/**",
            "/api/v1/public/property/**",
            "/ws/**",
            "/api/v1/chat/**",
            "/initiate/**",
            "/message/**",
            "/chat/conversation/**",
            "/api/v1/blog/get/**",
            "/api/v1/blog/search/**",
    };

    public static final String[] PROPERTY_CRUD_LIST = {
            "/api/v1/house/**",
            "/api/v1/flat/**",
            "/api/v1/room/**",
    };

    public static final String[] USERS_LIST = {
            "/api/v1/user/**"
    };

    public static final String[] OWNER_LIST = {
            "/api/v1/owner/**",
    };

    public static final String[] USER_OWNER_ADMIN_SHARED_LIST = {
            "/api/v1/user/{userName}",
            "/api/v1/user/update/password"
    };

    public static final String[] ADMIN_LIST = {
            "/api/v1/admin/**",
            "/api/v1/blog/create/**",
            "/api/v1/blog/update/**",
            "/api/v1/blog/delete/**",
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:uploads/images/");
        registry.addResourceHandler("/uploads/videos/**")
                .addResourceLocations("file:uploads/videos/");
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // DaoAuthenticationProvider uses UserPrincipal (returned by loadUserByUsername)
    // for authentication — no changes needed here
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userServiceImplementation);
        provider.setPasswordEncoder(passwordConfig.encoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PUBLIC_WHITELIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new SmsRateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public SecurityFilterChain userOwnerSharedFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(USER_OWNER_ADMIN_SHARED_LIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyAuthority("USER", "OWNER"))
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userServiceImplementation, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthException()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(USERS_LIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyAuthority("USER"))
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userServiceImplementation, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthException()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain ownerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(OWNER_LIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyAuthority("OWNER"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new RateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userServiceImplementation, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthException()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(ADMIN_LIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyAuthority("ADMIN"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userServiceImplementation, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthException()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain propertyFilter(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PROPERTY_CRUD_LIST)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyAuthority("OWNER"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userServiceImplementation, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimiterFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthException()));
        return http.build();
    }
}