package com.madremasa.controller;

import com.madremasa.model.Asignacion;
import com.madremasa.model.CierreVenta;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.ProductoRepositorio;
import com.madremasa.service.CierreService;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

// Cierres de jornada (RF-21, RF-22). El empleado de punto de venta registra el cierre de su punto asignado;
// el administrador consulta todos los cierres y puede corregirlos.
@Controller
public class CierreController {

    private final CierreVentaRepositorio cierreRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final CierreService cierreService;
    private final ControlAcceso controlAcceso;

    public CierreController(CierreVentaRepositorio cierreRepositorio, ProductoRepositorio productoRepositorio,
                            CierreService cierreService, ControlAcceso controlAcceso) {
        this.cierreRepositorio = cierreRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.cierreService = cierreService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping("/cierres")
    public String cierres(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PUNTO_VENTA);
        if (acceso != null) return acceso;

        if (ControlAcceso.ADMINISTRADOR.equals(controlAcceso.rol(sesion))) {
            modelo.addAttribute("cierres", cierreRepositorio.findAllByOrderByFechaDesc());
            return "cierres/lista";
        }
        modelo.addAttribute("cierres",
                cierreRepositorio.findByUsuarioCodigoEmpleadoOrderByFechaDesc(controlAcceso.codigoUsuario(sesion)));
        Asignacion asignacion = cierreService.asignacionDeHoy(controlAcceso.idUsuario(sesion)).orElse(null);
        modelo.addAttribute("asignacion", asignacion);
        if (asignacion != null && !cierreService.yaCerrado(asignacion)) {
            modelo.addAttribute("productos", productoRepositorio.findByActivoTrueOrderByNombreAsc());
        }
        return "cierres/lista";
    }

    // Registra el cierre de jornada del punto asignado hoy (solo rol punto de venta)
    @PostMapping("/cierres/registrar")
    public String registrar(@RequestParam(required = false) BigDecimal efectivo,
                            @RequestParam(required = false) BigDecimal datafono,
                            @RequestParam(required = false) List<Integer> idProducto,
                            @RequestParam(required = false) List<Integer> unidades,
                            HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.PUNTO_VENTA);
        if (acceso != null) return acceso;

        try {
            CierreVenta cierre = cierreService.registrar(controlAcceso.idUsuario(sesion), efectivo, datafono,
                    idProducto, unidades);
            redireccion.addFlashAttribute("exito", "El cierre de " + cierre.getPuntoVenta().getNombre()
                    + " se registró correctamente.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cierres";
    }

    // RF-22: formulario de corrección de un cierre (solo administrador)
    @GetMapping("/cierres/{id}/editar")
    public String editar(@PathVariable Integer id, HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        CierreVenta cierre = cierreRepositorio.findById(id).orElse(null);
        if (cierre == null) {
            redireccion.addFlashAttribute("error", "El cierre no existe.");
            return "redirect:/cierres";
        }
        // Unidades actuales por producto; los productos que no estaban en el cierre aparecen en cero
        java.util.Map<Integer, Integer> unidades = new java.util.HashMap<>();
        cierre.getDetalles().forEach(d -> unidades.put(d.getProducto().getIdProducto(), d.getUnidadesVendidas()));
        modelo.addAttribute("cierre", cierre);
        modelo.addAttribute("productos", productoRepositorio.findByActivoTrueOrderByNombreAsc());
        modelo.addAttribute("unidades", unidades);
        return "cierres/formulario";
    }

    @PostMapping("/cierres/{id}/corregir")
    public String corregir(@PathVariable Integer id,
                           @RequestParam(required = false) BigDecimal efectivo,
                           @RequestParam(required = false) BigDecimal datafono,
                           @RequestParam(required = false) List<Integer> idProducto,
                           @RequestParam(required = false) List<Integer> unidades,
                           HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            CierreVenta cierre = cierreService.corregir(id, efectivo, datafono, idProducto, unidades);
            redireccion.addFlashAttribute("exito", "El cierre de " + cierre.getPuntoVenta().getNombre()
                    + " se corrigió correctamente.");
            return "redirect:/cierres";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/cierres/" + id + "/editar";
        }
    }
}
