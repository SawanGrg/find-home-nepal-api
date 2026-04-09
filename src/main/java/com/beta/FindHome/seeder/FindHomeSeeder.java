package com.beta.FindHome.seeder;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.RoleStatus;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;

@Profile("dev")
@Configuration
public class FindHomeSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HouseRepository houseRepository;
    private final AmenitiesRepository amenitiesRepository;
    private final AreaRepository areaRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    public FindHomeSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            HouseRepository houseRepository,
            AmenitiesRepository amenitiesRepository,
            AreaRepository areaRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.houseRepository = houseRepository;
        this.amenitiesRepository = amenitiesRepository;
        this.areaRepository = areaRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Bean
    public ApplicationRunner findHomeSeed() {
        return args -> {
            if (!"dev".equalsIgnoreCase(activeProfile)) {
                System.out.println("Skipping database seeding, profile is not 'dev'");
                return;
            }
            try {
                seedRoles();
                seedUsers();
                seedHousesWithAmenities();
                System.out.println("Database seeding completed successfully.");
            } catch (Exception e) {
                System.err.println("Error during database seeding: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // =====================================================================
    // ROLES
    // =====================================================================

    private void seedRoles() {
        transactionTemplate.execute(status -> {
            createRoleIfNotExists("ADMIN");
            createRoleIfNotExists("OWNER");
            createRoleIfNotExists("USER");
            return null;
        });
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
            System.out.println("Created role: " + roleName);
        }
    }

    // =====================================================================
    // USERS
    // =====================================================================

    private void seedUsers() {
        transactionTemplate.execute(status -> {
            createUserIfNotExists("admin@example.com", "Admin", "User", "admin", "ADMIN");
            createUserIfNotExists("owner@example.com", "Owner", "User", "owner", "OWNER");
            createUserIfNotExists("user@example.com", "Regular", "User", "user", "USER");
            return null;
        });
    }

    private void createUserIfNotExists(String email, String firstName,
                                       String lastName, String username, String roleName) {
        if (userRepository.findByEmail(email).isPresent()) return;

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        Users user = Users.builder()
                .userName(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .dob(java.time.LocalDate.now().minusYears(20))
                .password(passwordEncoder.encode("password"))
                .isVerified(true)
                .maritalStatus(MaritalStatus.SINGLE)
                .roleStatus(RoleStatus.EMPLOYED)
                .userGender(Gender.MALE)
                .roles(Collections.singleton(role))
                .build();

        userRepository.save(user);
        System.out.println("Created user: " + email);
    }

    // =====================================================================
    // HOUSES
    // =====================================================================

    private void seedHousesWithAmenities() {
        if (houseRepository.count() > 0) {
            System.out.println("Houses already seeded. Skipping...");
            return;
        }

        Users landlord = userRepository.findByEmail("owner@example.com")
                .orElseThrow(() -> new RuntimeException("Landlord user not found"));

        for (int i = 1; i <= 3; i++) {
            final int index = i;
            transactionTemplate.execute(status -> {
                Users freshLandlord = userRepository.findById(landlord.getId())
                        .orElseThrow(() -> new RuntimeException("Landlord not found"));

                // House — uses Property base fields now
                House house = new House();
                house.setLandlord(freshLandlord);
                house.setPrice(BigDecimal.valueOf(500 + (index * 100L)));
                house.setDescription("Spacious and comfortable house " + index);
                house.setAddress(new Address(
                        "District " + index,
                        "City " + index,
                        "Ward " + index,
                        "Tole " + index
                ));
                house.setRules("No smoking, no pets allowed.");
                house.setAvailable(true);
                house.setVerified(index % 2 == 0);
                house.setDeleted(false);
                // House-specific fields
                house.setFloors(1 + index);
                house.setBedRooms(2 + index);
                house.setBathRooms(1 + index);
                house.setKitchen(1);
                house.setLivingRoom(1);

                House savedHouse = houseRepository.save(house);

                // Amenities — single property reference
                Amenities amenities = Amenities.builder()
                        .property(savedHouse)
                        .hasParking(index % 2 == 0)
                        .hasWifi(true)
                        .hasSecurityStaff(index % 3 == 0)
                        .hasUnderGroundWaterTank(index % 2 != 0)
                        .hasTV(true)
                        .hasCCTV(true)
                        .hasAC(index % 2 == 0)
                        .hasFridge(true)
                        .hasBalcony(index % 3 != 0)
                        .hasWater(true)
                        .hasSolarWaterHeater(true)
                        .hasFan(true)
                        .build();

                amenitiesRepository.save(amenities);

                // Area — single property reference
                Area area = new Area();
                area.setProperty(savedHouse);
                area.setLength(20.0f + index);
                area.setBreadth(15.0f + index);
                area.setTotalArea(area.getLength() * area.getBreadth());

                areaRepository.save(area);

                System.out.println("Seeded house " + index + " with ID: " + savedHouse.getId());
                return null;
            });
        }
    }
}