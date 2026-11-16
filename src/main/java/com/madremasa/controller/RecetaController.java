package com.madremasa.controller;

import com.madremasa.model.Receta;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.service.RecetaService;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

// Recetas y costeo (RF-13, RF-14, RF-15). Producción consulta sin costos; el administrador registra y edita.
@Controller
@RequestMapping("/recetas")
public class RecetaController {

    private final RecetaRepositorio recetaRepositorio;
    private final InsumoRepositorio insumoRepositorio;
    private final RecetaService recetaService;
    private final ControlAcceso controlAcceso;

    public RecetaController(RecetaRepositorio recetaRepositorio, InsumoRepositorio insumoRepositorio,
                            RecetaService recetaService, ControlAcceso controlAcceso) {
        this.recetaRepositorio = recetaRepositorio;
        this.insumoRepositorio = insumoRepositorio;
        this.recetaService = recetaService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping
    public String lista(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        modelo.addAttribute("recetas", recetaRepositorio.buscarTodasConDetalles());
        modelo.addAttribute("costos", recetaService);
        return "recetas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        modelo.addAttribute("insumos", insumoRepositorio.findAllByOrderByNombreAsc());
        return "recetas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombreProducto, @RequestParam(required = false) BigDecimal precio,
                          @RequestParam(required = false) Integer rendimiento,
                          @RequestParam(required = false) String instrucciones,
                          @RequestParam(required = false) Integer idInsumo,
                          @RequestParam(required = false) BigDecimal cantidad,
                          HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            Receta receta = recetaService.crear(nombreProducto, precio, rendimiento, instrucciones, idInsumo, cantidad);
            redireccion.addFlashAttribute("exito", "La receta se registró. Agregue el resto de los ingredientes.");
            return "redirect:/recetas/" + receta.getIdReceta();
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("insumos", insumoRepositorio.findAllByOrderByNombreAsc());
            modelo.addAttribute("nombreProducto", nombreProducto);
            modelo.addAttribute("precio", precio);
            modelo.addAttribute("rendimiento", rendimiento);
            modelo.addAttribute("instrucciones", instrucciones);
            return "recetas/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, HttpSession sesion, Model modelo,
                          RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        Receta receta = recetaRepositorio.findById(id).orElse(null);
        if (receta == null) {
            redireccion.addFlashAttribute("error", "La receta solicitada no existe.");
            return "redirect:/recetas";
        }
        modelo.addAttribute("receta", receta);
        modelo.addAttribute("costos", recetaService);
        modelo.addAttribute("insumos", insumoRepositorio.findAllByOrderByNombreAsc());
        return "recetas/detalle";
    }

    // Cambia el precio de venta, el rendimiento y las instrucciones
    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id, @RequestParam(required = false) BigDecimal precio,
                             @RequestParam(required = false) Integer rendimiento,
                             @RequestParam(required = false) String instrucciones,
                             HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            recetaService.actualizar(id, precio, rendimiento, instrucciones);
            redireccion.addFlashAttribute("exito", "Los datos de la receta se actualizaron.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas/" + id;
    }

    @PostMapping("/{id}/ingredientes")
    public String guardarIngrediente(@PathVariable Integer id, @RequestParam(required = false) Integer idInsumo,
                                     @RequestParam(required = false) BigDecimal cantidad,
                                     HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            recetaService.guardarIngrediente(id, idInsumo, cantidad);
            redireccion.addFlashAttribute("exito", "El ingrediente se guardó en la receta.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas/" + id;
    }

    @PostMapping("/{id}/ingredientes/{idInsumo}/quitar")
    public String quitarIngrediente(@PathVariable Integer id, @PathVariable Integer idInsumo,
                                    HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            recetaService.quitarIngrediente(id, idInsumo);
            redireccion.addFlashAttribute("exito", "El ingrediente se quitó de la receta.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas/" + id;
    }
}
