package ca.immotran.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Securise l'API avec des jetons JWT emis par le MEME User Pool Cognito
 * qu'immotran-ms-identity.
 *
 * Regles :
 *  - /actuator/health et /actuator/info restent ouverts (sondes de sante,
 *    load balancer -- ils ne doivent jamais dependre d'un jeton)
 *  - tout le reste exige un jeton valide : en-tete "Authorization: Bearer <jwt>"
 *
 * Ce service ne fait AUCUN appel reseau vers immotran-ms-identity : il
 * verifie lui-meme la signature du jeton via les cles publiques (JWKS)
 * de Cognito, puis lit directement le claim tenant_id/tenant_scope
 * (voir TenantClaims). C'est le principe decrit dans le README
 * d'immotran-ms-identity : chaque microservice fait confiance au tenant_id
 * du jeton, sans jamais reimplementer la logique des roles.
 *
 * Profil "local" (voir localJwtDecoder ci-dessous) : meme bascule que
 * dans immotran-ms-identity -- obligatoire de lancer les DEUX services
 * avec --spring.profiles.active=local pour que le frontend puisse
 * s'authentifier avant qu'un vrai User Pool Cognito existe.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Profile("!local")
    public JwtDecoder jwtDecoder(
            @Value("${immotran.cognito.region}") String region,
            @Value("${immotran.cognito.user-pool-id}") String userPoolId) {

        String jwkSetUri = "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json"
                .formatted(region, userPoolId);

        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    @Profile("local")
    public JwtDecoder localJwtDecoder(@Value("${immotran.local-auth.shared-secret}") String sharedSecret) {
        SecretKeySpec secretKey = new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
