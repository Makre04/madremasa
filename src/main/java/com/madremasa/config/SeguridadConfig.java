package com.madremasa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Inicio de sesión con código de empleado y control de acceso por rol (RF-01, RF-02, RNF-02)
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(acceso -> acceso
                .requestMatchers("/css/**", "/login", "/error").permitAll()
                .requestMatchers("/usuarios/**", "/ventas/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/insumos/nuevo", "/insumos/guardar", "/insumos/*/editar").hasRole("ADMINISTRADOR")
                .requestMatchers("/insumos/**", "/recetas/**", "/produccion/**")
                    .hasAnyRole("ADMINISTRADOR", "PRODUCCION")
                .requestMatchers("/cierres/**").hasAnyRole("ADMINISTRADOR", "PUNTO_VENTA")
                .anyRequest().authenticated())
            .formLogin(formulario -> formulario
                .loginPage("/login")
                .usernameParameter("codigo")
                .passwordParameter("contrasena")
                .defaultSuccessUrl("/panel", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(salida -> salida
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salida")
                .permitAll())
            .exceptionHandling(error -> error.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }

    // Las contraseñas se comparan contra el hash BCrypt guardado (RNF-01)
    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }
}
