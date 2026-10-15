-- Catálogo seed (spec 002). Precios con 2 decimales exactos; hay un producto con
-- stock bajo (id 6) y uno sin stock (id 8) para la demo.
INSERT INTO products (id, name, category, price, stock, version) VALUES
    (1, 'Auriculares Bluetooth', 'TECHNOLOGY',  59.90, 10, 0),
    (2, 'Teclado Mecánico',      'TECHNOLOGY',  89.00,  8, 0),
    (3, 'Monitor 27"',           'TECHNOLOGY', 249.00,  3, 0),
    (4, 'Cargador USB-C',        'TECHNOLOGY',  19.90, 25, 0),
    (5, 'Lámpara de Escritorio', 'HOME',        34.50, 12, 0),
    (6, 'Taza de Cerámica',      'HOME',         9.99,  2, 0),
    (7, 'Camiseta de Algodón',   'CLOTHING',    24.00, 20, 0),
    (8, 'Zapatillas Running',    'CLOTHING',    79.00,  0, 0);
