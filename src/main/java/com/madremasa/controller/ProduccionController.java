package com.madremasa.controller;

import com.madremasa.dto.Necesidad;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.service.ProduccionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// Simulación de producción y verificación de faltantes (RF-17, RF-19, RF-20)
@Controller
@RequestMapping("/produccion")
public class ProduccionController {

    private final RecetaRepositorio recetaRepositorio;
    private final ProduccionRepositorio produccionRepositorio;
    private final ProduccionService produccionService;

    public ProduccionController(RecetaRepositorio recetaRepositorio, ProduccionRepositorio produccionRepositorio,
                                ProduccionService produccionService) {
        this.recetaRepositorio = recetaRepositorio;
        this.produccionRepositorio = produccionRepositorio;
        this.produccionService = produccionService;
    }

    @GetMapping
    public String formulario(Model modelo) {
        cargarDatos(modelo);
        return "produccion/simular";
    }

    @PostMapping("/simular")
    public String simular(@RequestParam(required = false) Integer idReceta,
                          @RequestParam(required = false) Integer tandas, Model modelo) {
        cargarDatos(modelo);
        modelo.addAttribute("idReceta", idReceta);
        modelo.addAttribute("tandas", tandas);

        if (idReceta == null || tandas == null || tandas < 1 || tandas > 50) {
            modelo.addAttribute("error", "Seleccione una receta e indique entre 1 y 50 tandas.");
            return "produccion/simular";
        }

        List<Necesidad> necesidades = produccionService.simular(idReceta, tandas);
        modelo.addAttribute("necesidades", necesidades);
        modelo.addAttribute("alcanza", necesidades.stream().allMatch(Necesidad::alcanza));
        return "produccion/simular";
    }

    private void cargarDatos(Model modelo) {
        modelo.addAttribute("recetas", recetaRepositorio.buscarTodasConDetalles());
        modelo.addAttribute("producciones", produccionRepositorio.findTop5ByOrderByFechaDesc());
    }
}
