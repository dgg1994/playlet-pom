package com.onetoken.utils.oidc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OidcIdTokenPayload {
    private String sub;
    private String email;
    private Boolean emailVerified;
}
