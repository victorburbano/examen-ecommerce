package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JpaRefreshTokenRepositoryAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES = NOW.plusSeconds(3600);

    @Mock
    private RefreshTokenJpaRepository jpaRepository;

    private JpaRefreshTokenRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaRefreshTokenRepositoryAdapter(jpaRepository);
    }

    @Test
    void saveMapsTheDomainTokenAndReturnsTheAssignedId() {
        when(jpaRepository.save(any(RefreshTokenJpaEntity.class))).thenAnswer(invocation -> {
            RefreshTokenJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 2L);
            return entity;
        });

        RefreshToken saved = adapter.save(RefreshToken.issue("hash", "family-1", 5L, NOW, EXPIRES));

        assertThat(saved.id()).isEqualTo(2L);
        ArgumentCaptor<RefreshTokenJpaEntity> entity = ArgumentCaptor.forClass(RefreshTokenJpaEntity.class);
        verify(jpaRepository).save(entity.capture());
        assertThat(entity.getValue().getTokenHash()).isEqualTo("hash");
        assertThat(entity.getValue().getFamilyId()).isEqualTo("family-1");
        assertThat(entity.getValue().getUserId()).isEqualTo(5L);
        assertThat(entity.getValue().getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getValue().getExpiresAt()).isEqualTo(EXPIRES);
    }

    @Test
    void findByTokenHashMapsEveryField() {
        when(jpaRepository.findByTokenHash("hash")).thenReturn(Optional.of(entity()));

        Optional<RefreshToken> found = adapter.findByTokenHash("hash");

        assertThat(found).contains(
                new RefreshToken(2L, "hash", "family-1", 5L, NOW, EXPIRES, NOW.plusSeconds(10), "hash-next"));
    }

    @Test
    void findByTokenHashReturnsEmptyWhenMissing() {
        when(jpaRepository.findByTokenHash("unknown")).thenReturn(Optional.empty());

        assertThat(adapter.findByTokenHash("unknown")).isEmpty();
    }

    @Test
    void markRotatedIsTrueOnlyWhenExactlyOneRowWasUpdated() {
        when(jpaRepository.markRotated(2L, "hash-next", NOW)).thenReturn(1);
        when(jpaRepository.markRotated(3L, "hash-next", NOW)).thenReturn(0);

        assertThat(adapter.markRotated(2L, "hash-next", NOW)).isTrue();
        assertThat(adapter.markRotated(3L, "hash-next", NOW)).isFalse();
    }

    @Test
    void revokeFamilyReturnsTheNumberOfRevokedTokens() {
        when(jpaRepository.revokeFamily("family-1", NOW)).thenReturn(3);

        assertThat(adapter.revokeFamily("family-1", NOW)).isEqualTo(3);
    }

    private static RefreshTokenJpaEntity entity() {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
                "hash", "family-1", 5L, NOW, EXPIRES, NOW.plusSeconds(10), "hash-next");
        ReflectionTestUtils.setField(entity, "id", 2L);
        return entity;
    }
}
