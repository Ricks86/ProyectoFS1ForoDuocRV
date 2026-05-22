package com.ms.Auth.Security;

import com.ms.Auth.Model.UserAuth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String SECRET_KEY = "Nosoygayperosoyperuanoytengounafantasiaendondeperuinvadechileychiletienequeexportar";
    private static final long EXPIRATION_TIME = 86400000;

    public String generateToken(UserAuth user){
        Map<String,Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getNombreUser())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
