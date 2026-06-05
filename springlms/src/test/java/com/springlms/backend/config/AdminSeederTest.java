package com.springlms.backend.config;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        adminSeeder = new AdminSeeder(userRepository, passwordEncoder);
    }

    @Test
    void createsAdminWhenMissing() {
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminSeeder.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedUser.getState()).isEqualTo(State.ACTIVE);
        assertThat(savedUser.getEmail()).startsWith("admin_").endsWith("@springlms.local");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    void doesNotCreateAdminWhenOneExists() {
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(User.builder().build()));

        adminSeeder.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
