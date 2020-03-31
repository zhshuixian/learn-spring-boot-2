package org.xian.token.secutiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.xian.token.entity.SysUser;

import java.io.Serializable;
import java.util.Date;


/**
 * 生成 验证用户Token
 *
 * @author xian
 */
@Component
public class TokenUtils implements Serializable{
    private static final long serialVersionUID = -3L;
    /**
     * Token 有效时长
     */
    private static final Long EXPIRATION = 604800L;

    /**
     * 生成的 Token 字符串 setIssuer 签发者 setAudience 接收者 setExpiration 过期时间 setIssuedAt 签发时间
     *
     * @param sysUser 用户信息
     * @return 生成的Token字符串 or null
     */
    public String createToken(SysUser sysUser) {
        try {
            Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
            String token = Jwts.builder()
                    .setIssuer("SpringBoot")
                    .setAudience(sysUser.getUsername())
                    .setExpiration(expirationDate)
                    .setIssuedAt(new Date())
                    .claim("role", sysUser.getUserRole())
                    .signWith(RsaUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();
            return String.format("Bearer %s", token);
        } catch (Exception e) {
            return null;
        }
    }


    public SysUser validationToken(String token) {
        System.out.println("ddddd");
        try {
            // 解密 Token，获取 Claims 主体
            Claims claims = Jwts.parserBuilder().
                    setSigningKey(RsaUtils.getPublicKey()).
                    build().parseClaimsJws(token).getBody();
            assert claims != null;
            System.out.println("33333");
            // 验证 Token 有没有过期 过期时间
            Date expiration = claims.getExpiration();
            // 判断是否过期 过期时间要在当前日期之后
            if (!expiration.after(new Date())) {
                return null;
            }
            System.out.println(claims.get("role").toString());
            SysUser sysUser = new SysUser();
            sysUser.setUsername(claims.getAudience());
            sysUser.setUserRole(claims.get("role").toString());
            return sysUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }


}

