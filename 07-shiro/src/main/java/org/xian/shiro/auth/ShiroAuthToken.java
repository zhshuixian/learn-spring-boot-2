package org.xian.shiro.auth;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author xian
 */
public class ShiroAuthToken implements AuthenticationToken {
    private String token;

    public ShiroAuthToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
