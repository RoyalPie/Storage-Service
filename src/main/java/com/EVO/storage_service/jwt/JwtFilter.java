package com.EVO.storage_service.jwt;


import com.EVO.storage_service.entity.Permission;
import com.EVO.storage_service.entity.Role;
import com.EVO.storage_service.entity.User;
import com.EVO.storage_service.repository.UserRepository;
import com.EVO.storage_service.security.CustomAuthenticationToken;
import com.EVO.storage_service.service.JwtTokenBlackListService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtTokenBlackListService blackListService;

    private final JwtDecoder jwtDecoder;

    Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (isKeycloakToken(token)) {
                chain.doFilter(request, response);
                return;
            }

            try {
                String email = jwtUtils.extractEmail(token);
                if (email != null && jwtUtils.validateToken(token, email) && !blackListService.isBlacklisted(token)) {
                    User authenticatedUser = userRepository.findByEmailWithRolesAndPermissions(email).orElseThrow(() -> new UsernameNotFoundException("Not found User with that email"));
                    Set<Role> roles = authenticatedUser.getRoles();
                    Set<Permission> permissions = roles.stream().flatMap(role -> role.getPermissions().stream()).collect(Collectors.toSet());
                    Boolean isRoot = roles.stream().anyMatch(Role::getIsRoot);
                    CustomAuthenticationToken authentication =
                            new CustomAuthenticationToken(email, null, null, roles, permissions, isRoot);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {

                logger.error("Invalid token or username", e);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isKeycloakToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return "http://localhost:8080/realms/testing-realm".equals(decodedJWT.getIssuer());
        } catch (Exception e) {
            return false;
        }
    }

}