package com.ecommerce.auth.infrastructure;

import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.port.UserRepository;
import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto de dominio sobre Spring Data JPA. */
@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final Clock clock;

    public JpaUserRepositoryAdapter(UserJpaRepository jpaRepository, Clock clock) {
        this.jpaRepository = jpaRepository;
        this.clock = clock;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(user.email(), user.passwordHash(), user.role(), clock.instant());
        return toDomain(jpaRepository.save(entity));
    }

    private static User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
    }
}
