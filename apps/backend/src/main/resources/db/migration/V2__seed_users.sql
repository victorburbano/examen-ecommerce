-- Usuarios de prueba (contraseñas documentadas en el README). Los hashes BCrypt se
-- generaron con BCryptPasswordEncoder (strength 10, prefijo $2a$).
INSERT INTO users (email, password_hash, role, created_at)
VALUES ('admin@example.com', '$2a$10$xKH178ieiazE/otPsjBcb.If6j1Kllc0HBWUpTof.3No08AkGUvrS', 'ADMIN', CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, role, created_at)
VALUES ('user@example.com', '$2a$10$MvFcYHALft/d5JyCB8o/Y.JZTOfs3QldjlK/3WBPlET5f25b3p/b6', 'USER', CURRENT_TIMESTAMP);
