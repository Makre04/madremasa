package com.madremasa.controller;

import com.madremasa.model.Receta;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.service.RecetaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Consulta de recetas y su costo (RF-13, RF-14, RF-15)
@Controller
@RequestMapping("/recetas")
public class RecetaController {

    private final RecetaRepositorio recetaRepositorio;
    private final RecetaService recetaService;

    public RecetaController(RecetaRepositorio recetaRepositorio, RecetaService recetaService) {
        this.recetaRepositorio = recetaRepositorio;
        this.recetaService = recetaService;
    }

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("recetas", recetaRepositorio.buscarTodasConDetalles());
        modelo.addAttribute("costos", recetaService);
        return "recetas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model modelo, RedirectAttributes redireccion) {
        Receta receta = recetaRepositorio.findById(id).orElse(null);
        if (receta == null) {
            redireccion.addFlashAttribute("error", "La receta solicitada no existe.");
            return "redirect:/recetas";
        }
        modelo.addAttribute("receta", receta);
        modelo.addAttribute("costos", recetaService);
        return "recetas/detalle";
    }
}
