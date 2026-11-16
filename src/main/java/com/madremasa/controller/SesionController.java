package com.madremasa.controller;

import com.madremasa.model.Usuario;
import com.madremasa.repository.UsuarioRepositorio;
import com.madremasa.service.UsuarioService;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Inicio y cierre de sesión con HttpSession (RF-01, RF-03, RNF-03)
@Controller
public class SesionController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioService usuarioService;
    private final ControlAcceso controlAcceso;

    public SesionController(UsuarioRepositorio usuarioRepositorio, UsuarioService usuarioService,
                            ControlAcceso controlAcceso) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.usuarioService = usuarioService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping("/login")
    public String login(HttpSession sesion) {
        // Si ya hay una sesión iniciada, no se vuelve a pedir el código
        if (controlAcceso.verificar(sesion) == null) {
            return "redirect:/panel";
        }
        return "login";
    }

    @PostMapping("/login")
    public String ingresar(@RequestParam String codigo, @RequestParam String contrasena,
                           HttpServletRequest solicitud, HttpSession sesion) {
        String codigoNormalizado = codigo.trim().toUpperCase();
        Usuario usuario = usuarioRepositorio.findByCodigoEmpleado(codigoNormalizado).orElse(null);

        // RNF-03: una cuenta bloqueada no puede ingresar hasta que pasen los 15 minutos
        if (usuario != null && usuarioService.estaBloqueado(usuario)) {
            return "redirect:/login?bloqueado";
        }
        // RN-02: solo los usuarios activos pueden ingresar. El mensaje no indica cuál dato falló.
        if (usuario == null || !usuario.getActivo() || !usuarioService.contrasenaCorrecta(usuario, contrasena)) {
            if (usuario != null && usuario.getActivo()) {
                usuarioService.registrarIntentoFallido(usuario);
                if (usuarioService.estaBloqueado(usuario)) {
                    return "redirect:/login?bloqueado";
                }
            }
            return "redirect:/login?error";
        }
        usuarioService.registrarIngresoCorrecto(usuario);

        // Se cambia el identificador de la sesión al ingresar para evitar la fijación de sesión
        solicitud.changeSessionId();
        // Se guardan en la sesión el identificador, el código, el nombre y el rol del usuario
        sesion.setAttribute(ControlAcceso.USUARIO_ID, usuario.getIdUsuario());
        sesion.setAttribute(ControlAcceso.USUARIO_CODIGO, usuario.getCodigoEmpleado());
        sesion.setAttribute(ControlAcceso.USUARIO_NOMBRE, usuario.getNombreCompleto());
        sesion.setAttribute(ControlAcceso.USUARIO_ROL, usuario.getRol().getNombre());
        return "redirect:/panel";
    }

    // Cierre de sesión: se elimina toda la información guardada en la sesión
    @PostMapping("/logout")
    public String salir(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login?salida";
    }
}
