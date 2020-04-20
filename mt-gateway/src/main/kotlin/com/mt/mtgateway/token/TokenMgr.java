package com.mt.mtgateway.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt.mtgateway.bean.Role;
import com.mt.mtgateway.bean.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签发和验证token的类
 */
public class TokenMgr {
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private static final ObjectMapper json = new ObjectMapper();
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

    /**
     * 精简角色
     *
     * @return 精简后的模式，这样生成的token更短
     */
    public static Object[] simplify(Collection<Role> roles) {
        Object[] data = new Object[roles.size()];
        int i = 0;
        for (Role role : roles) {
            data[i++] = new Object[]{role.getName().replace("ROLE_", ""), role.getCompanyid()};
        }
        return data;
    }

    // TODO 不再序列化role的全部公司id
    public static String createJWT(User user) throws IOException {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + Constant.JWT_TTL;
        Date now = new Date(nowMillis);
        Date expDate = new Date(expMillis);
        Claims claims = Jwts.claims();
        claims.put("id", user.getId());
        //claims.put("username", user.getUsername());
        Object[] role = simplify(user.getRoles());
        claims.put("roles", json.writeValueAsString(role));
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
