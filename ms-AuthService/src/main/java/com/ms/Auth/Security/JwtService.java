package com.ms.Auth.Security;

import com.ms.Auth.Model.UserAuth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String SECRET_KEY = "Nosoygayperosoyperuanoytengounaf";//32
    private static final long EXPIRATION_TIME = 86400000;

    public String generateToken(UserAuth user){
        Map<String,Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getNombreUser())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firmamos con nuestra llave
                .compact();
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        if (keyBytes.length < 32) {
            // Si la clave es muy corta, tirará error. Usamos texto plano si no es Base64
            return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
