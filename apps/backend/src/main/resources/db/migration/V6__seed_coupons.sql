-- Cupones seed (spec 004). Porcentajes exactos con 2 decimales. MEGA40 (40 %) permite demostrar
-- el tope del 35 % con un carrito grande; EXPIRED2025 sirve para probar el error de cupón vencido.
INSERT INTO coupons (code, percent, valid_from, valid_to) VALUES
    ('WELCOME2026', 15.00, DATE '2026-01-01', DATE '2026-12-31'),
    ('EXPIRED2025', 10.00, DATE '2025-01-01', DATE '2025-12-31'),
    ('MEGA40',      40.00, DATE '2026-01-01', DATE '2026-12-31');
