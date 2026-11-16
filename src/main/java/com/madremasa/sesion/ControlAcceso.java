package com.madremasa.sesion;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

// Control de acceso con HttpSession: cada controlador lo llama antes de ejecutar una operación (RF-02)
@Component
public class ControlAcceso {

    // Nombres de los atributos que se guardan en la sesión al iniciar sesión
    public static final String USUARIO_ID = "usuarioId";
    public static final String USUARIO_CODIGO = "usuarioCodigo";
    public static final String USUARIO_NOMBRE = "usuarioNombre";
    public static final String USUARIO_ROL = "usuarioRol";

    // Roles registrados en la tabla rol
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String PRODUCCION = "PRODUCCION";
    public static final String PUNTO_VENTA = "PUNTO_VENTA";

    // Devuelve null si el usuario puede continuar. Si no hay sesión, envía al inicio de sesión;
    // si el rol no tiene permiso, envía a la página de acceso denegado.
    // Sin roles indicados, basta con tener una sesión iniciada.
    public String verificar(HttpSession sesion, String... rolesPermitidos) {
        Object rol = sesion.getAttribute(USUARIO_ROL);
        if (rol == null) {
            return "redirect:/login";
        }
        if (rolesPermitidos.length == 0) {
            return null;
        }
        for (String permitido : rolesPermitidos) {
            if (permitido.equals(rol)) {
                return null;
            }
        }
        return "redirect:/acceso-denegado";
    }

    // Código del empleado que inició sesión
    public String codigoUsuario(HttpSession sesion) {
        return (String) sesion.getAttribute(USUARIO_CODIGO);
    }

    // Identificador del empleado que inició sesión
    public Integer idUsuario(HttpSession sesion) {
        return (Integer) sesion.getAttribute(USUARIO_ID);
    }

    // Rol del empleado que inició sesión
    public String rol(HttpSession sesion) {
        return (String) sesion.getAttribute(USUARIO_ROL);
    }
}
