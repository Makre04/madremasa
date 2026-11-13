package com.madremasa.controller;

import com.madremasa.dto.InsumoFormulario;
import com.madremasa.model.Insumo;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.UnidadMedidaRepositorio;
import com.madremasa.service.InsumoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Consulta, registro y edición de insumos (RF-08, RF-11, RF-12)
@Controller
@RequestMapping("/insumos")
public class InsumoController {

    private final InsumoRepositorio insumoRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;
    private final InsumoService insumoService;

    public InsumoController(InsumoRepositorio insumoRepositorio, UnidadMedidaRepositorio unidadRepositorio,
                            InsumoService insumoService) {
        this.insumoRepositorio = insumoRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.insumoService = insumoService;
    }

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("insumos", insumoRepositorio.findAllByOrderByNombreAsc());
        return "insumos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("insumo", new InsumoFormulario());
        modelo.addAttribute("unidades", unidadRepositorio.findAll());
        return "insumos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes redireccion) {
        Insumo insumo = insumoRepositorio.findById(id).orElse(null);
        if (insumo == null) {
            redireccion.addFlashAttribute("error", "El insumo solicitado no existe.");
            return "redirect:/insumos";
        }
        modelo.addAttribute("insumo", insumoService.aFormulario(insumo));
        modelo.addAttribute("unidades", unidadRepositorio.findAll());
        return "insumos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("insumo") InsumoFormulario formulario, BindingResult errores,
                          Model modelo, RedirectAttributes redireccion) {
        if (!errores.hasFieldErrors("nombre") && insumoService.nombreRepetido(formulario)) {
            errores.rejectValue("nombre", "repetido", "Ya existe un insumo con ese nombre");
        }
        if (errores.hasErrors()) {
            modelo.addAttribute("unidades", unidadRepositorio.findAll());
            return "insumos/formulario";
        }
        Insumo guardado = insumoService.guardar(formulario);
        redireccion.addFlashAttribute("exito", "El insumo \"" + guardado.getNombre() + "\" se guardó correctamente.");
        return "redirect:/insumos";
    }
}
