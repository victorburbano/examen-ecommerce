package com.ecommerce.checkout.api;

import com.ecommerce.checkout.application.OrderQueryService;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.PageResult;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Órdenes del usuario. {@code USER} ve solo las suyas; {@code ADMIN} ve todas. Pedir una orden
 * ajena devuelve 404 (lo decide el caso de uso, no {@code @PreAuthorize}).
 */
@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final CheckoutMapper checkoutMapper;

    public OrderController(OrderQueryService orderQueryService, CheckoutMapper checkoutMapper) {
        this.orderQueryService = orderQueryService;
        this.checkoutMapper = checkoutMapper;
    }

    @GetMapping
    public PageResponse<OrderResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Requester requester = Requester.from(jwt);
        PageResult<Order> orders =
                orderQueryService.listForRequester(requester.userId(), requester.admin(), page, size);
        return checkoutMapper.toPageResponse(orders);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Requester requester = Requester.from(jwt);
        Order order = orderQueryService.getForRequester(id, requester.userId(), requester.admin());
        return checkoutMapper.toOrderResponse(order);
    }
}
