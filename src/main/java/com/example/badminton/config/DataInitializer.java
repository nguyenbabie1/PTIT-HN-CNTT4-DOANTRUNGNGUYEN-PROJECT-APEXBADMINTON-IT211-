package com.example.badminton.config;

import com.example.badminton.entity.Court;
import com.example.badminton.entity.Role;
import com.example.badminton.entity.TimeSlot;
import com.example.badminton.entity.User;
import com.example.badminton.repository.CourtRepository;
import com.example.badminton.repository.TimeSlotRepository;
import com.example.badminton.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("System Admin")
                        .email("admin@apexbadminton.com")
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build());

                userRepository.save(User.builder()
                        .username("manager")
                        .password(passwordEncoder.encode("manager123"))
                        .fullName("Court Manager")
                        .email("manager@apexbadminton.com")
                        .role(Role.ROLE_MANAGER)
                        .enabled(true)
                        .build());

                userRepository.save(User.builder()
                        .username("customer")
                        .password(passwordEncoder.encode("customer123"))
                        .fullName("Demo Customer")
                        .email("customer@apexbadminton.com")
                        .role(Role.ROLE_CUSTOMER)
                        .enabled(true)
                        .build());
            }

            if (courtRepository.count() == 0) {
                courtRepository.save(Court.builder()
                        .name("Sân số 1")
                        .active(true)
                        .pricePerHour(new BigDecimal("150000"))
                        .build());
                courtRepository.save(Court.builder()
                        .name("Sân số 2")
                        .active(true)
                        .pricePerHour(new BigDecimal("180000"))
                        .build());
            }

            if (timeSlotRepository.count() == 0) {
                timeSlotRepository.save(TimeSlot.builder()
                        .label("08:00 - 10:00")
                        .startTime(LocalTime.of(8, 0))
                        .endTime(LocalTime.of(10, 0))
                        .build());
                timeSlotRepository.save(TimeSlot.builder()
                        .label("10:00 - 12:00")
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(12, 0))
                        .build());
                timeSlotRepository.save(TimeSlot.builder()
                        .label("14:00 - 16:00")
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(16, 0))
                        .build());
            }
        };
    }
}
