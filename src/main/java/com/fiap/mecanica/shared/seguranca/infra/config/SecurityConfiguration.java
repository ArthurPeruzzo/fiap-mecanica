package com.fiap.mecanica.shared.seguranca.infra.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserAuthenticationFilter userAuthenticationFilter;

    protected static final String ADMINISTRADOR = "ADMINISTRADOR";
    protected static final String ATENDENTE = "ATENDENTE";
    protected static final String MECANICO = "MECANICO";

    static final String[] ENDPOINTS_SEM_AUTENTICACAO = {
            "/authenticate/login",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/favicon.ico"
    };

    private static final String[] ENDPOINTS_ADMINISTRADOR = {
            "/cliente/**",
            "/veiculo/**",
            "/peca/**",
            "/insumo/**",
            "/servico/**",
            "/ordem-servico/detalhamento"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ENDPOINTS_SEM_AUTENTICACAO).permitAll()
                        .requestMatchers(ENDPOINTS_ADMINISTRADOR).hasRole(ADMINISTRADOR)
                        // ATENDENTE
                        .requestMatchers(HttpMethod.POST,  "/ordem-servico").hasRole(ATENDENTE)
                        .requestMatchers(HttpMethod.POST,  "/ordem-servico/orcamento/**").hasRole(ATENDENTE)
                        .requestMatchers(HttpMethod.PATCH, "/ordem-servico/*/entregar").hasRole(ATENDENTE)
                        // MECANICO
                        .requestMatchers(HttpMethod.PATCH,  "/ordem-servico/*/diagnostico").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PATCH,  "/ordem-servico/*/diagnostico/conclusao").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PUT,    "/ordem-servico/*/servicos/*").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.DELETE, "/ordem-servico/*/servicos/*").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PATCH,  "/ordem-servico/*/servicos/*/iniciar").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PATCH,  "/ordem-servico/*/servicos/*/finalizar").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PUT,    "/ordem-servico/*/pecas/*").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.DELETE, "/ordem-servico/*/pecas/*").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.PUT,    "/ordem-servico/*/insumos/*").hasRole(MECANICO)
                        .requestMatchers(HttpMethod.DELETE, "/ordem-servico/*/insumos/*").hasRole(MECANICO)
                        // QUALQUER PERFIL AUTENTICADO
                        .requestMatchers(HttpMethod.GET, "/ordem-servico/*/status").hasAnyRole(ATENDENTE, MECANICO, ADMINISTRADOR)
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Não autenticado\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Acesso negado\"}");
                        })
                )
                .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
