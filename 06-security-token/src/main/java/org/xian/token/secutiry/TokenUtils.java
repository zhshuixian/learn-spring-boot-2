package org.xian.token.secutiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;


/**
 * 生成 验证用户Token
 *
 * @author xian
 */
@Component
public class TokenUtils implements Serializable {

    private static final long serialVersionUID = -2312282442273493553L;
    /**
     * Token 有效时长
     */
    private static final Long EXPIRATION = 604800L;

    /**
     * 生成的 Token 字符串 setIssuer 签发者 setAudience 接收者 setExpiration 过期时间 setIssuedAt 签发时间
     *
     * @param userDetails userDetails
     * @return 生成的Token字符串 or null
     */
    public String createToken(UserDetails userDetails) {
        try {
            return String.format("Bearer %s",
                    Jwts.builder().setIssuer("Scorpio").setAudience(userDetails.getUsername())
                            .setExpiration(expirationDate()).setIssuedAt(new Date())
                            .signWith(RsaUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                            .compact());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据Token 验证Token是否有效
     *
     * @param token       Token
     * @param userDetails 用户信息
     * @return Token是否有效 有效返回True
     */
    boolean validationToken(final String token, final String username, final UserDetails userDetails) {
        try {
            // final var authUserDetails = (AuthUserDetails) userDetails;
            // 用户名是否对 是否未过期
            return (username.equals(userDetails.getUsername()) && isNotExpired(token));
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * 解密Token 获取到claims
     *
     * @param token Token字符串
     * @return Token claims or null
     */
    private Claims getClaims(String token) {
        try {
            // return Jwts.parser().setSigningKey(RsaUtils.getPublicKey()).parseClaimsJws(token).getBody();
            return Jwts.parserBuilder().setSigningKey(RsaUtils.getPublicKey()).build().parseClaimsJwt(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Token过期时间
     *
     * @return 过期时间
     */
    private Date expirationDate() {
        return new Date(System.currentTimeMillis() + EXPIRATION * 1000);
    }

    /**
     * 从Token获取到用户名 即Token的接收者
     *
     * @param token Token
     * @return username or null
     */
    String getUsername(String token) {
        try {
            final var claims = getClaims(token);
            assert claims != null;
            return claims.getAudience();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证Token是否过期 修改密码后过期 时间过后过期
     *
     * @param token Token
     * @param flag  上次修改密码的时间
     * @return 未过期返回True
     */
    private boolean isNotExpired(String token, Date flag) {
        try {
            final var claims = getClaims(token);
            assert claims != null;
            // 过期时间
            final var expiration = claims.getExpiration();
            // Token创建时间
            final var created = claims.getIssuedAt();
            // 判断是否过期
            return (expiration.after(new Date()) && created.after(flag));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNotExpired(String token) {
        try {
            final var claims = getClaims(token);
            assert claims != null;
            // 过期时间
            final var expiration = claims.getExpiration();
            // Token创建时间
            final var created = claims.getIssuedAt();
            // 判断是否过期
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

