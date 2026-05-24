package com.ms.User.Security;

import com.ms.User.Config.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthentication jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("api/users/init").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/users/username/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/users/id/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
