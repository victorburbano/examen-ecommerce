package com.ecommerce.checkout.application;

import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.port.ProductRepository;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.port.OrderRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unidad atómica de la compra (una transacción): recalcula, descuenta el stock de todas las
 * líneas y persiste la orden. Si algo falla (stock insuficiente, conflicto de versión o error al
 * guardar) la transacción se revierte entera, así que nunca queda stock descontado sin orden.
 */
@Service
public class CheckoutTransaction {

    private final CheckoutCalculation checkoutCalculation;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public CheckoutTransaction(
            CheckoutCalculation checkoutCalculation,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            Clock clock) {
        this.checkoutCalculation = checkoutCalculation;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    @Transactional
    public Order place(Long userId, CheckoutCommand command) {
        PricedCart priced = checkoutCalculation.price(command);
        reserveStock(priced);

        Order order = Order.place(
                userId,
                priced.cart(),
                priced.breakdown(),
                priced.coupon().map(AppliedCoupon::code).orElse(null),
                UUID.randomUUID(),
                clock.instant());
        orderRepository.save(order);
        return order;
    }

    private void reserveStock(PricedCart priced) {
        List<CartLine> lines = priced.cart().lines();
        List<Product> products = priced.products();
        for (int index = 0; index < lines.size(); index++) {
            Product product = products.get(index);
            // decreaseStock lanza InsufficientStockException si ya no alcanza (409, sin reintento);
            // save fuerza el flush y puede lanzar OptimisticLockingFailureException (se reintenta).
            productRepository.save(product.decreaseStock(lines.get(index).quantity()));
        }
    }
}
