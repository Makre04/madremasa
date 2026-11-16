package com.madremasa.sesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.SecureRandom;
import java.util.HexFormat;

// Protección contra CSRF sin Spring Security (RNF-05).
// Cada sesión tiene un token aleatorio; todos los formularios POST lo envían en el campo oculto "_csrf"
// y la solicitud se rechaza si no coincide con el de la sesión.
@Component
@ControllerAdvice
public class ProteccionCsrf implements HandlerInterceptor {

    public static final String ATRIBUTO = "csrfToken";
    public static final String PARAMETRO = "_csrf";

    private final SecureRandom aleatorio = new SecureRandom();

    // Pone el token de la sesión a disposición de todas las vistas como ${csrfToken}
    @ModelAttribute(ATRIBUTO)
    public String token(HttpSession sesion) {
        String token = (String) sesion.getAttribute(ATRIBUTO);
        if (token == null) {
            byte[] bytes = new byte[32];
            aleatorio.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            sesion.setAttribute(ATRIBUTO, token);
        }
        return token;
    }

    // Antes de cada POST compara el token enviado con el de la sesión
    @Override
    public boolean preHandle(HttpServletRequest solicitud, HttpServletResponse respuesta, Object controlador)
            throws Exception {
        if (!"POST".equalsIgnoreCase(solicitud.getMethod())) {
            return true;
        }
        HttpSession sesion = solicitud.getSession(false);
        String esperado = sesion == null ? null : (String) sesion.getAttribute(ATRIBUTO);
        String recibido = solicitud.getParameter(PARAMETRO);
        if (esperado != null && esperado.equals(recibido)) {
            return true;
        }
        // Token ausente o vencido (por ejemplo, la página quedó abierta después de reiniciar la aplicación)
        respuesta.sendRedirect(solicitud.getContextPath() + "/login?expirada");
        return false;
    }
}
