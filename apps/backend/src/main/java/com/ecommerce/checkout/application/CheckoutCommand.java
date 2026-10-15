package com.ecommerce.checkout.application;

import java.util.List;

/**
 * Petición de cotización/compra: los ítems pedidos y un código de cupón opcional.
 * Los precios y totales siempre se recalculan en el servidor.
 */
public record CheckoutCommand(List<RequestedItem> items, String couponCode) {
}
