package com.springlms.backend.config;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void run(String... args) {
        boolean adminExists = !userRepository.findByRole(Role.ADMIN).isEmpty();

        if (adminExists) {
            return;
        }

        String username = "admin_" + randomString(5);
        String rawPassword = randomString(8);
        String email = username + "@springlms.local";

        User admin = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.ADMIN)
                .state(State.ACTIVE)
                .build();

        userRepository.save(admin);

        System.out.println();
        System.out.println("==============================================");
        System.out.println(" INITIAL ADMIN CREATED");
        System.out.println(" Username: " + username);
        System.out.println(" Email:    " + email);
        System.out.println(" Password: " + rawPassword);
        System.out.println(" This is shown only because no admin existed.");
        System.out.println("==============================================");
        System.out.println();
    }

    private String randomString(int length) {
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < length; i++) {
            value.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
        }

        return value.toString();
    }
}