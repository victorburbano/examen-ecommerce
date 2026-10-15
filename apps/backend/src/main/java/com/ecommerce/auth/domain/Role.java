package com.ecommerce.auth.domain;

/**
 * Roles del sistema. El registro público siempre crea {@link #USER}; {@link #ADMIN} solo por seed.
 */
public enum Role {
    USER,
    ADMIN
}
