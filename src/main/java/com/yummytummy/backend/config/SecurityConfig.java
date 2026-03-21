package com.yummytummy.backend.config;

import com.yummytummy.backend.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${allowed.origins:http://localhost:3000,http://localhost:3001,http://localhost:3002}")
    private String allowedOrigins;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, AuthenticationProvider authenticationProvider) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Apply CORS configuration from the bean
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Allow all OPTIONS requests for CORS preflight
                .requestMatchers(org.springframework.web.bind.annotation.RequestMethod.OPTIONS.name()).permitAll()
                
                // Public endpoints first to ensure they are accessible
                .requestMatchers("/api/auth/**", "/api/auth/verify-email", "/api/menu", "/uploads/**", "/api/offers", "/api/offers/**", "/api/staff/**", "/api/support/**", "/error").permitAll()
                
                // Protected endpoints
                .requestMatchers("/api/user/**", "/api/cart/**").hasAnyAuthority("ROLE_USER", "USER")
                .requestMatchers("/api/orders/**").authenticated()
                
                // Admin Management
                .requestMatchers("/api/admin/analytics/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/products/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/categories/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/support/messages/**").hasAuthority("ROLE_ADMIN")
                
                // Offers: GET is public (handled above), others need Admin
                .requestMatchers("/api/offers/**").hasAuthority("ROLE_ADMIN")

                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
