package com.ecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.UserNotFoundException;
import com.ecommerce.auth.domain.port.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void returnsTheUserOfTheAccessToken() {
        User user = new User(5L, "user@example.com", "hash", Role.USER);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        User current = new GetCurrentUserService(userRepository).currentUser(5L);

        assertThat(current).isEqualTo(user);
    }

    @Test
    void failsWhenTheUserWasRemoved() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetCurrentUserService(userRepository).currentUser(5L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
