package com.ecommerce.auth.application;

import com.ecommerce.auth.domain.Email;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.EmailAlreadyTakenException;
import com.ecommerce.auth.domain.port.PasswordHasher;
import com.ecommerce.auth.domain.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro público: normaliza el email, comprueba unicidad y hashea la contraseña. */
@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RegisterService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public User register(String rawEmail, String rawPassword) {
        String email = Email.normalize(rawEmail);
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }
        return userRepository.save(User.newUser(email, passwordHasher.hash(rawPassword)));
    }
}
