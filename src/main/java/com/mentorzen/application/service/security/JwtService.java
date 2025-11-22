package com.mentorzen.application.service.security;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import com.mentorzen.domain.entity.User;
import com.mentorzen.infrastructure.config.properties.JwtProperties;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String encode(User user) {
        final Instant now = Instant.now();

        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.getExpireDuration()))
                .build();

        final JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
