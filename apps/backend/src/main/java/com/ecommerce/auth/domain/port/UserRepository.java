package com.ecommerce.auth.domain.port;

import com.ecommerce.auth.domain.User;
import java.util.Optional;

/** Puerto de persistencia de usuarios (implementado por el adaptador JPA). */
public interface UserRepository {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);
}
