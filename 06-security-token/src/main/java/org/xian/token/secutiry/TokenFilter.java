package org.xian.token.secutiry;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xian.token.secutiry.TokenUtils;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Jwt 拦截器，通过Token鉴权
 *
 * @author xian
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Component
public class TokenFilter extends OncePerRequestFilter {

    @Resource
    @Qualifier("authUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Resource
    private TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // 存储Token的Headers Key与Value
        final var authorizationKey = "Authorization";
        String authorizationValue;
        try {
            authorizationValue = request.getHeader(authorizationKey);
        } catch (Exception e) {
            authorizationValue = null;
        }
        // Token 开头部分
        final var bearer = "Bearer ";
        if (authorizationValue != null && authorizationValue.startsWith(bearer)) {
            // token
            final var token = authorizationValue.substring(bearer.length());
            final var username = tokenUtils.getUsername(token);
            // 验证Token // 通过查询数据的方式
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (tokenUtils.validationToken(token, username, userDetails)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(userDetails.getUsername());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
