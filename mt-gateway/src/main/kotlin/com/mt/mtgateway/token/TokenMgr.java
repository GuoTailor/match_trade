package com.mt.mtgateway.token;

import com.mt.mtgateway.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 签发和验证token的类
 */
public class TokenMgr {
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    /**
     * 创建SecretKey
     */
    private static final Key key = Keys.hmacShaKeyFor(Constant.JWT_SECRET.getBytes());

    /**
     * 签发JWT/
     */
    public static String createJWT(String id, String subject, String role, String issuer, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        System.out.println("主题" + subject + "   " + role);
        JwtBuilder builder = Jwts.builder()
                .setId(id)                                          // JWT_ID
                .setSubject(subject)                                // 主题
                .setAudience(role)                                  // 接受者
                .setIssuer(issuer)                                  // 签发者
                .setNotBefore(new Date())                           // 开始时间
                .setIssuedAt(now)                                   // 签发时间
                .signWith(key, signatureAlgorithm);           // 签名算法以及密匙
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date expDate = new Date(expMillis);
            builder.setExpiration(expDate);                         // 失效时间
        }
        return builder.compact();
    }

    public static String createJWT(User user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + Constant.JWT_TTL;
        Date now = new Date(nowMillis);
        Date expDate = new Date(expMillis);
        Claims claims = Jwts.claims();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRoles());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key, signatureAlgorithm)          // 签名算法以及密匙
                .setNotBefore(now)                          // 设置在此之前不能用
                .setExpiration(expDate)                     // 设置在此之后不能用
                .compact();
    }

    /**
     * 验证JWT
     */
    public static CheckPOJO validateJWT(String jwtStr) {
        CheckPOJO checkResult = new CheckPOJO();
        Claims claims;
        try {
            claims = parseJWT(jwtStr);
            checkResult.setSuccess(true);
            checkResult.setClaims(claims);
        } catch (ExpiredJwtException e) {
            checkResult.setErrCode(Constant.JWT_ERRCODE_EXPIRE);
            checkResult.setSuccess(false);
        } catch (SecurityException e) {
            checkResult.setErrCode(Constant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        } catch (Exception e) {
            checkResult.setErrCode(Constant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        }
        return checkResult;
    }

    /**
     * 解析JWT字符串
     */
    private static Claims parseJWT(String jws) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws)
                .getBody();
    }

}
