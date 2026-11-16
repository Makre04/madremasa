package com.madremasa.controller;

import com.madremasa.model.Usuario;
import com.madremasa.repository.RolRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.service.UsuarioService;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Gestión de empleados: consultar, registrar, editar, restablecer contraseña y activar o desactivar
// (RF-04, RF-05). Todo el módulo es exclusivo del administrador.
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final UsuarioService usuarioService;
    private final ControlAcceso controlAcceso;

    public UsuarioController(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio,
                             UsuarioService usuarioService, ControlAcceso controlAcceso) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.usuarioService = usuarioService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping
    public String lista(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        modelo.addAttribute("usuarios", usuarioRepositorio.findAllByOrderByCodigoEmpleadoAsc());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        modelo.addAttribute("roles", rolRepositorio.findAll());
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        Usuario usuario = usuarioRepositorio.findById(id).orElse(null);
        if (usuario == null) {
            redireccion.addFlashAttribute("error", "El empleado solicitado no existe.");
            return "redirect:/usuarios";
        }
        modelo.addAttribute("usuario", usuario);
        modelo.addAttribute("roles", rolRepositorio.findAll());
        return "usuarios/formulario";
    }

    // Registra (sin id) o actualiza (con id) un empleado
    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Integer idUsuario,
                          @RequestParam(required = false) String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) Integer idRol,
                          @RequestParam(required = false) String contrasena,
                          HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            Usuario usuario = idUsuario == null
                    ? usuarioService.crear(codigo, nombre, idRol, contrasena)
                    : usuarioService.actualizar(idUsuario, nombre, idRol, contrasena);
            redireccion.addFlashAttribute("exito", "El empleado " + usuario.getCodigoEmpleado() + " se guardó correctamente.");
            return "redirect:/usuarios";
        } catch (ReglaNegocioException e) {
            // Se vuelve al formulario con los datos escritos y el mensaje de error
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("usuario", idUsuario == null ? null : usuarioRepositorio.findById(idUsuario).orElse(null));
            modelo.addAttribute("codigo", codigo);
            modelo.addAttribute("nombre", nombre);
            modelo.addAttribute("idRol", idRol);
            modelo.addAttribute("roles", rolRepositorio.findAll());
            return "usuarios/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            Usuario usuario = usuarioService.cambiarEstado(id, controlAcceso.idUsuario(sesion));
            redireccion.addFlashAttribute("exito", "El empleado " + usuario.getCodigoEmpleado() + " quedó "
                    + (usuario.getActivo() ? "activo." : "inactivo."));
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
