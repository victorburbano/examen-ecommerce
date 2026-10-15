package com.ecommerce.checkout.domain.exception;

/**
 * No se pudo reservar el stock tras agotar los reintentos ante conflictos de versión
 * (409 INSUFFICIENT_STOCK). Se traduce al mismo código que un stock insuficiente: para el
 * cliente el resultado es el mismo, otro comprador se quedó las unidades.
 */
public class StockConflictException extends RuntimeException {

    public StockConflictException() {
        super("Stock could not be reserved after several attempts");
    }
}
