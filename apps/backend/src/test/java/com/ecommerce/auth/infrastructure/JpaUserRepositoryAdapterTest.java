package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private UserJpaRepository jpaRepository;

    private JpaUserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaUserRepositoryAdapter(jpaRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void existsByEmailDelegatesToSpringData() {
        when(jpaRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThat(adapter.existsByEmail("user@example.com")).isTrue();
    }

    @Test
    void findByEmailMapsTheEntityToTheDomain() {
        when(jpaRepository.findByEmail("user@example.com")).thenReturn(Optional.of(entity(3L)));

        Optional<User> found = adapter.findByEmail("user@example.com");

        assertThat(found).contains(new User(3L, "user@example.com", "hash", Role.USER));
    }

    @Test
    void findByEmailReturnsEmptyWhenThereIsNoUser() {
        when(jpaRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail("none@example.com")).isEmpty();
    }

    @Test
    void findByIdMapsTheEntityToTheDomainOrEmpty() {
        when(jpaRepository.findById(3L)).thenReturn(Optional.of(entity(3L)));
        when(jpaRepository.findById(4L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(3L)).contains(new User(3L, "user@example.com", "hash", Role.USER));
        assertThat(adapter.findById(4L)).isEmpty();
    }

    @Test
    void saveStampsTheCreationInstantAndReturnsTheAssignedId() {
        when(jpaRepository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> {
            UserJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 11L);
            return entity;
        });

        User saved = adapter.save(User.newUser("user@example.com", "hash"));

        assertThat(saved.id()).isEqualTo(11L);
        ArgumentCaptor<UserJpaEntity> entity = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(jpaRepository).save(entity.capture());
        assertThat(entity.getValue().getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getValue().getRole()).isEqualTo(Role.USER);
    }

    private static UserJpaEntity entity(Long id) {
        UserJpaEntity entity = new UserJpaEntity("user@example.com", "hash", Role.USER, NOW);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
