package com.ecommerce.checkout.api;

import com.ecommerce.checkout.application.CheckoutService;
import com.ecommerce.checkout.application.QuoteService;
import com.ecommerce.checkout.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints del checkout. Sin lógica de negocio: delega en los casos de uso. */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final QuoteService quoteService;
    private final CheckoutService checkoutService;
    private final CheckoutMapper checkoutMapper;

    public CheckoutController(
            QuoteService quoteService, CheckoutService checkoutService, CheckoutMapper checkoutMapper) {
        this.quoteService = quoteService;
        this.checkoutService = checkoutService;
        this.checkoutMapper = checkoutMapper;
    }

    /** Cotización pública: recalcula siempre en el servidor y no tiene efectos secundarios. */
    @PostMapping("/quote")
    public BreakdownResponse quote(@Valid @RequestBody CheckoutRequest request) {
        return checkoutMapper.toBreakdownResponse(quoteService.quote(checkoutMapper.toCommand(request)));
    }

    /** Compra autenticada: valida stock, lo descuenta y persiste la orden en una transacción. */
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderResponse order(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CheckoutRequest request) {
        Order order = checkoutService.checkout(Requester.from(jwt).userId(), checkoutMapper.toCommand(request));
        return checkoutMapper.toOrderResponse(order);
    }
}
