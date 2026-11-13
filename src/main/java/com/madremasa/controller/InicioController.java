package com.madremasa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Vistas generales: inicio de sesión, acceso denegado y guía de mensajes
@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/panel";
    }

    // El envío del formulario (POST /login) lo procesa Spring Security
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }

    // Guía de mensajes del sistema creada en la I Prueba Parcial
    @GetMapping("/mensajes")
    public String mensajes() {
        return "mensajes";
    }
}
