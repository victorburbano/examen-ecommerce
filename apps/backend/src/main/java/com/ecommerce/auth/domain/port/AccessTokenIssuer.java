package com.ecommerce.auth.domain.port;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.User;

/** Puerto de emisión de access tokens (JWT HS256 en infraestructura). */
public interface AccessTokenIssuer {

    AccessToken issue(User user);
}
