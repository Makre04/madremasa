package com.madremasa.sesion;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Registra la revisión del token CSRF para todas las rutas de la aplicación
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final ProteccionCsrf proteccionCsrf;

    public ConfiguracionWeb(ProteccionCsrf proteccionCsrf) {
        this.proteccionCsrf = proteccionCsrf;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(proteccionCsrf).addPathPatterns("/**").excludePathPatterns("/css/**");
    }
}
