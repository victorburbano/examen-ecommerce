package com.ecommerce.checkout.application;

import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.catalog.domain.port.ProductRepository;
import com.ecommerce.checkout.domain.Coupon;
import com.ecommerce.checkout.domain.CouponCode;
import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountPipelineFactory;
import com.ecommerce.checkout.domain.discount.exception.EmptyCartException;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import com.ecommerce.checkout.domain.exception.CouponNotFoundException;
import com.ecommerce.checkout.domain.port.CouponRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Caso de cálculo compartido por {@code quote} y {@code orders}: reconstruye el carrito desde la
 * base de datos (nunca confía en precios del cliente), resuelve el cupón y aplica el motor.
 * No valida stock: eso es cosa de la compra (spec 004).
 */
@Component
public class CheckoutCalculation {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final DiscountPipelineFactory discountPipelineFactory;
    private final Clock clock;

    public CheckoutCalculation(
            ProductRepository productRepository,
            CouponRepository couponRepository,
            DiscountPipelineFactory discountPipelineFactory,
            Clock clock) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.discountPipelineFactory = discountPipelineFactory;
        this.clock = clock;
    }

    public PricedCart price(CheckoutCommand command) {
        List<RequestedItem> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new EmptyCartException();
        }
        rejectDuplicates(items);

        Map<Long, Product> productsById = loadProducts(items);
        List<CartLine> lines = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        for (RequestedItem item : items) {
            Product product = productsById.get(item.productId());
            if (product == null) {
                throw new ProductNotFoundException(item.productId());
            }
            products.add(product);
            lines.add(new CartLine(
                    product.id(), product.name(), product.category(), product.price(), item.quantity()));
        }

        Cart cart = new Cart(lines);
        Optional<AppliedCoupon> coupon = resolveCoupon(command.couponCode());
        DiscountBreakdown breakdown = discountPipelineFactory.create(coupon).calculate(cart);
        return new PricedCart(cart, List.copyOf(products), coupon, breakdown);
    }

    private Map<Long, Product> loadProducts(List<RequestedItem> items) {
        List<Long> ids = items.stream().map(RequestedItem::productId).toList();
        return productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::id, Function.identity(), (first, second) -> first, LinkedHashMap::new));
    }

    private Optional<AppliedCoupon> resolveCoupon(String rawCode) {
        Optional<String> code = CouponCode.normalize(rawCode);
        if (code.isEmpty()) {
            return Optional.empty();
        }
        Coupon coupon = couponRepository
                .findByCode(code.get())
                .orElseThrow(() -> new CouponNotFoundException(code.get()));
        coupon.assertUsableOn(LocalDate.now(clock));
        return Optional.of(coupon.toAppliedCoupon());
    }

    private static void rejectDuplicates(List<RequestedItem> items) {
        Set<Long> seen = new HashSet<>();
        for (RequestedItem item : items) {
            if (!seen.add(item.productId())) {
                throw new InvalidCartException("duplicate productId in cart: " + item.productId());
            }
        }
    }
}
