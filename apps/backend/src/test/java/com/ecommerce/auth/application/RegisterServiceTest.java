package com.ecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.EmailAlreadyTakenException;
import com.ecommerce.auth.domain.port.PasswordHasher;
import com.ecommerce.auth.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Test
    void normalizesTheEmailHashesThePasswordAndSavesAUserRole() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordHasher.hash("User123!")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.<User>getArgument(0).withId(10L));
        RegisterService service = new RegisterService(userRepository, passwordHasher);

        User registered = service.register("  User@Example.COM ", "User123!");

        assertThat(registered.id()).isEqualTo(10L);
        assertThat(registered.email()).isEqualTo("user@example.com");
        assertThat(registered.passwordHash()).isEqualTo("bcrypt-hash");
        assertThat(registered.role()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsAnAlreadyRegisteredEmailWithoutHashing() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        RegisterService service = new RegisterService(userRepository, passwordHasher);

        assertThatThrownBy(() -> service.register("user@example.com", "User123!"))
                .isInstanceOf(EmailAlreadyTakenException.class);

        verify(passwordHasher, never()).hash(any());
        verify(userRepository, never()).save(any());
    }
}
