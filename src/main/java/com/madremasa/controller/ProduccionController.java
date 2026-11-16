package com.madremasa.controller;

import com.madremasa.dto.Necesidad;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.model.Produccion;
import com.madremasa.service.ProduccionService;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// Simulación, confirmación e historial de producción (RF-16 a RF-20)
@Controller
@RequestMapping("/produccion")
public class ProduccionController {

    private final RecetaRepositorio recetaRepositorio;
    private final ProduccionRepositorio produccionRepositorio;
    private final ProduccionService produccionService;
    private final ControlAcceso controlAcceso;

    public ProduccionController(RecetaRepositorio recetaRepositorio, ProduccionRepositorio produccionRepositorio,
                                ProduccionService produccionService, ControlAcceso controlAcceso) {
        this.recetaRepositorio = recetaRepositorio;
        this.produccionRepositorio = produccionRepositorio;
        this.produccionService = produccionService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping
    public String formulario(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        cargarDatos(modelo);
        return "produccion/simular";
    }

    @PostMapping("/simular")
    public String simular(@RequestParam(required = false) Integer idReceta,
                          @RequestParam(required = false) Integer tandas, HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        cargarDatos(modelo);
        modelo.addAttribute("idReceta", idReceta);
        modelo.addAttribute("tandas", tandas);

        if (idReceta == null || tandas == null || tandas < 1 || tandas > ProduccionService.MAXIMO_TANDAS) {
            modelo.addAttribute("error", "Seleccione una receta e indique entre 1 y 50 tandas.");
            return "produccion/simular";
        }

        List<Necesidad> necesidades = produccionService.simular(idReceta, tandas);
        modelo.addAttribute("necesidades", necesidades);
        modelo.addAttribute("alcanza", necesidades.stream().allMatch(Necesidad::alcanza));
        return "produccion/simular";
    }

    // Confirma la producción y descuenta los insumos (RF-16, RF-18)
    @PostMapping("/confirmar")
    public String confirmar(@RequestParam(required = false) Integer idReceta,
                            @RequestParam(required = false) Integer tandas,
                            HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        try {
            Produccion produccion = produccionService.confirmar(idReceta, tandas == null ? 0 : tandas,
                    controlAcceso.idUsuario(sesion));
            redireccion.addFlashAttribute("exito", "Se confirmó la producción de " + produccion.getUnidadesProducidas()
                    + " unidades de " + produccion.getReceta().getProducto().getNombre()
                    + ". Los insumos se descontaron del inventario.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/produccion";
    }

    private void cargarDatos(Model modelo) {
        modelo.addAttribute("recetas", recetaRepositorio.buscarTodasConDetalles());
        // Historial completo por fecha, receta y responsable (RF-20)
        modelo.addAttribute("producciones", produccionRepositorio.findAllByOrderByFechaDesc());
    }
}
