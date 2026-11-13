package com.madremasa.controller;

import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Consulta de empleados registrados (RF-04)
@Controller
public class UsuarioController {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioController(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping("/usuarios")
    public String lista(Model modelo) {
        modelo.addAttribute("usuarios", usuarioRepositorio.findAllByOrderByCodigoEmpleadoAsc());
        return "usuarios/lista";
    }
}
