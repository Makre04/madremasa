package com.madremasa.controller;

import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Vistas generales: inicio, acceso denegado y guía de mensajes
@Controller
public class InicioController {

    private final ControlAcceso controlAcceso;

    public InicioController(ControlAcceso controlAcceso) {
        this.controlAcceso = controlAcceso;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/panel";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(HttpSession sesion) {
        String acceso = controlAcceso.verificar(sesion);
        if (acceso != null) return acceso;
        return "acceso-denegado";
    }

    // Guía de mensajes del sistema creada en la I Prueba Parcial
    @GetMapping("/mensajes")
    public String mensajes(HttpSession sesion) {
        String acceso = controlAcceso.verificar(sesion);
        if (acceso != null) return acceso;
        return "mensajes";
    }
}
