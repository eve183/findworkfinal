package com.findwork.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

   
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth siempre público
                        .requestMatchers("/api/auth/**").permitAll()

                        // Registro público de candidatos / empleadores
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                        // Lectura pública del catálogo de empleo (visitantes sin cuenta)
                        .requestMatchers(HttpMethod.GET,
                                "/api/ofertas/**",
                                "/api/categorias/**",
                                "/api/resenas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/empresas/empleador/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/empresas/**").permitAll()

                        // Publicación de ofertas y empresas: empleadores o administrador
                        .requestMatchers(HttpMethod.POST,
                                "/api/ofertas/**", "/api/empresas/**")
                        .hasAnyRole("EMPLEADOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/ofertas/**", "/api/empresas/**")
                        .hasAnyRole("EMPLEADOR", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/ofertas/**", "/api/empresas/**")
                        .hasAnyRole("EMPLEADOR", "ADMINISTRADOR")

                        // Catálogo de categorías: solo administración
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMINISTRADOR")

                        // Usuarios: consulta/edición autenticada (el servicio valida dueño/admin),
                        // el borrado queda reservado al administrador.
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/usuarios/**").authenticated()

                        // Roles: solo administración
                        .requestMatchers("/api/roles/**").hasRole("ADMINISTRADOR")

                        // Perfiles, postulaciones, reseñas y notificaciones: usuario logueado
                        .requestMatchers("/api/perfiles/**", "/api/postulaciones/**",
                                "/api/resenas/**", "/api/notificaciones/**").authenticated()

                        // Todo lo demás exige estar autenticado
                        .anyRequest().authenticated())
                // Basic se mantiene para pruebas/herramientas; el flujo principal es JWT.
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
