package org.xian.shiro.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.xian.shiro.entity.SysUser;

import java.io.Serializable;
import java.util.Date;


/**
 * 生成 验证用户Token
 *
 * @author xian
 */
@Component
public class TokenUtils implements Serializable {
    private static final long serialVersionUID = -3L;
    /**
     * Token 有效时长 多少秒
     */
    private static final Long EXPIRATION = 2 * 60L;

    /**
     * 生成 Token 字符串  setAudience 接收者 setExpiration 过期时间 role 用户角色
     *
     * @param sysUser 用户信息
     * @return 生成的Token字符串 or null
     */
    public String createToken(SysUser sysUser) {
        try {
            // Token 的过期时间
            Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
            // 生成 Token
            String token = Jwts.builder()
                    // 设置 Token 签发者 可选
                    .setIssuer("SpringBoot")
                    // 根据用户名设置 Token 的接受者
                    .setAudience(sysUser.getUsername())
                    // 设置过期时间
                    .setExpiration(expirationDate)
                    // 设置 Token 生成时间 可选
                    .setIssuedAt(new Date())
                    // 通过 claim 方法设置一个 key = role，value = userRole 的值
                    .claim("role", sysUser.getUserRole())
                    .claim("permission", sysUser.getUserPermission())
                    // 设置加密密钥和加密算法，注意要用私钥加密且保证私钥不泄露
                    .signWith(RsaUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();
            return String.format("Bearer %s", token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token ，并获取到用户名和用户权限信息
     *
     * @param token Token 字符串
     * @return sysUser 用户信息
     */
    public SysUser validationToken(String token) {
        try {
            // 解密 Token，获取 Claims 主体
            Claims claims = Jwts.parserBuilder()
                    // 设置公钥解密，以为私钥是保密的，因此 Token 只能是自己生成的，如此来验证 Token
                    .setSigningKey(RsaUtils.getPublicKey())
                    .build().parseClaimsJws(token).getBody();
            assert claims != null;
    /*          // 验证 Token 有没有过期 过期时间 过期时间会子判断
            Date expiration = claims.getExpiration();
            // 判断是否过期 过期时间要在当前日期之后

            // 下面一段代码可以不要，过期的 Token 会直接 throw ExpiredJwtException
            if (!expiration.after(new Date())) {
                return null;
            }*/
            // 获得用户信息
            SysUser sysUser = new SysUser();
            sysUser.setUsername(claims.getAudience());
            sysUser.setUserRole(claims.get("role").toString());
            sysUser.setUserPermission(claims.get("permission").toString());
            return sysUser;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Token 刷新
     *
     * @param token 就 Token
     * @return String 新 Token 或者 null
     */
    public String refreshToken(String token) {
        try {
            // 解密 Token，获取 Claims 主体
            Claims claims = Jwts.parserBuilder()
                    // 设置公钥解密，以为私钥是保密的，因此 Token 只能是自己生成的，如此来验证 Token
                    .setSigningKey(RsaUtils.getPublicKey())
                    .build().parseClaimsJws(token).getBody();
            // 刷新 Token 1 下面代码是未到期刷新
            // 可以更改代码，在验证的 Token 的时候直接判断是否要刷新 Token
            assert claims != null;
            // Token 过期时间
            Date expiration = claims.getExpiration();
            // 如果 1 分钟内过期，则刷新 Token
            if (!expiration.before(new Date(System.currentTimeMillis() + 60 * 1000))) {
                // 不用刷新
                return null;
            }
            SysUser sysUser = new SysUser();
            sysUser.setUsername(claims.getAudience());
            sysUser.setUserRole(claims.get("role").toString());
            sysUser.setUserPermission(claims.get("permission").toString());
            // 生成新的 Token
            return createToken(sysUser);
        } catch (ExpiredJwtException e) {
            // 刷新 Token 2 Token 在解密的时候会自动判断是否过期
            // 过期 ExpiredJwtException 可以通过 e.getClaims() 取得 claims
            // 实际中千万不要直接这么用，
            // TODO  需要自己用 RSA 算法验证 Token 的合法性
            try {
                Claims claims = e.getClaims();
                // 如果 claims 不为空表示合法的 Token
                assert claims != null;
                // Token 过期时间
                Date expiration = claims.getExpiration();
                // 如果过期时间在 10 分钟内，则刷新 Token
                if (!expiration.after(new Date(System.currentTimeMillis() - 10 * 60 * 1000))) {
                    // 超过 10 分钟，没得救了
                    return null;
                } else {
                    SysUser sysUser = new SysUser();
                    sysUser.setUsername(claims.getAudience());
                    sysUser.setUserRole(claims.get("role").toString());
                    sysUser.setUserPermission(claims.get("permission").toString());
                    return createToken(sysUser);
                }
            } catch (Exception e1) {
                return null;
            }

        }
    }

}
