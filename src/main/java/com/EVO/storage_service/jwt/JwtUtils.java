package com.EVO.storage_service.jwt;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {
    private final RSAKeyUtil rsaKeyUtil;

    public JwtUtils(RSAKeyUtil rsaKeyUtil) {
        this.rsaKeyUtil = rsaKeyUtil;
    }

    public Claims extractClaims(String token) throws Exception {
        PublicKey publicKey = rsaKeyUtil.getPublicKey();
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
    }

    public String extractEmail(String token) throws Exception {
        return extractClaims(token).getSubject();
    }

    public Date extractExpiration(String token) throws Exception {
        return extractClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) throws Exception {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String email) throws Exception {
        return (email.equals(extractEmail(token)) && !isTokenExpired(token));
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}