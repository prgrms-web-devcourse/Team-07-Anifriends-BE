package com.clova.anifriends.domain.auth.authentication;

import com.clova.anifriends.domain.auth.jwt.JwtProvider;
import com.clova.anifriends.domain.auth.dto.response.CustomClaims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final JwtProvider jwtProvider;

    public Authentication authenticate(String accessToken) {
        CustomClaims claims = jwtProvider.parseAccessToken(accessToken);
        JwtAuthentication authentication = new JwtAuthentication(claims.memberId(), claims.role(), accessToken);
        List<GrantedAuthority> authorities = getAuthorities(claims.authorities());
        return UsernamePasswordAuthenticationToken.authenticated(authentication, accessToken,
            authorities);
    }

    private List<GrantedAuthority> getAuthorities(List<String> authorities) {
        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
    }
}
