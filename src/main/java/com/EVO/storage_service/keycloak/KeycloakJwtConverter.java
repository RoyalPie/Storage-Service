package com.EVO.storage_service.keycloak;


import com.EVO.storage_service.entity.Permission;
import com.EVO.storage_service.entity.Role;
import com.EVO.storage_service.entity.User;
import com.EVO.storage_service.repository.UserRepository;
import com.EVO.storage_service.security.CustomAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, CustomAuthenticationToken> {
    @Autowired
    private UserRepository userRepository;
    private final JwtDecoder jwtDecoder;

    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public KeycloakJwtConverter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public CustomAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaim("email");
        User authenticatedUser = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found User with that email"));

        Set<Role> roles = authenticatedUser.getRoles();
        Set<Permission> permissions = roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
        Boolean isRoot = roles.stream().anyMatch(Role::getIsRoot);

        return CustomAuthenticationToken.authenticated(email, null, null, roles, permissions, isRoot);
    }

}
