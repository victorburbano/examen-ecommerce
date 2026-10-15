package com.ecommerce.checkout.application;

import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;

/**
 * Resultado de cotizar: el carrito reconstruido desde la base de datos, los productos leídos
 * (con su stock y versión, para decrementarlos al comprar) y el desglose del motor.
 */
public record PricedCart(
        Cart cart, List<Product> products, Optional<AppliedCoupon> coupon, DiscountBreakdown breakdown) {
}
