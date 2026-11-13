package com.madremasa.controller;

import com.madremasa.repository.CierreVentaRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Cierres de jornada registrados por el empleado de punto de venta (RF-21)
@Controller
public class CierreController {

    private final CierreVentaRepositorio cierreRepositorio;

    public CierreController(CierreVentaRepositorio cierreRepositorio) {
        this.cierreRepositorio = cierreRepositorio;
    }

    @GetMapping("/cierres")
    public String misCierres(Authentication autenticacion, Model modelo) {
        modelo.addAttribute("cierres",
                cierreRepositorio.findByUsuarioCodigoEmpleadoOrderByFechaDesc(autenticacion.getName()));
        return "cierres/lista";
    }
}
