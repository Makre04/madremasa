package com.madremasa.controller;

import com.madremasa.model.PuntoVenta;
import com.madremasa.repository.AsignacionRepositorio;
import com.madremasa.repository.PuntoVentaRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import com.madremasa.service.PuntoVentaService;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

// Puntos de venta y asignación de empleados (RF-06, RF-07). Exclusivo del administrador.
@Controller
@RequestMapping("/puntos")
public class PuntoVentaController {

    private final PuntoVentaRepositorio puntoRepositorio;
    private final AsignacionRepositorio asignacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PuntoVentaService puntoService;
    private final ControlAcceso controlAcceso;

    public PuntoVentaController(PuntoVentaRepositorio puntoRepositorio, AsignacionRepositorio asignacionRepositorio,
                                UsuarioRepositorio usuarioRepositorio, PuntoVentaService puntoService,
                                ControlAcceso controlAcceso) {
        this.puntoRepositorio = puntoRepositorio;
        this.asignacionRepositorio = asignacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.puntoService = puntoService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping
    public String lista(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        modelo.addAttribute("puntos", puntoRepositorio.findAllByOrderByNombreAsc());
        modelo.addAttribute("puntosActivos", puntoRepositorio.findByActivoTrueOrderByNombreAsc());
        modelo.addAttribute("vendedores",
                usuarioRepositorio.findByRolNombreAndActivoTrueOrderByCodigoEmpleadoAsc(ControlAcceso.PUNTO_VENTA));
        modelo.addAttribute("asignaciones", asignacionRepositorio.findAllByOrderByFechaDesc());
        modelo.addAttribute("hoy", LocalDate.now());
        return "puntos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession sesion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;
        return "puntos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        PuntoVenta punto = puntoRepositorio.findById(id).orElse(null);
        if (punto == null) {
            redireccion.addFlashAttribute("error", "El punto de venta solicitado no existe.");
            return "redirect:/puntos";
        }
        modelo.addAttribute("punto", punto);
        return "puntos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) Integer idPuntoVenta, @RequestParam String nombre,
                          @RequestParam(required = false) String tipo, @RequestParam String ubicacion,
                          @RequestParam(required = false) List<Integer> dias,
                          @RequestParam(defaultValue = "false") boolean activo,
                          HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            PuntoVenta punto = puntoService.guardar(idPuntoVenta, nombre, tipo, ubicacion, dias, activo);
            redireccion.addFlashAttribute("exito", "El punto de venta \"" + punto.getNombre() + "\" se guardó correctamente.");
            return "redirect:/puntos";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            if (idPuntoVenta != null) {
                modelo.addAttribute("punto", puntoRepositorio.findById(idPuntoVenta).orElse(null));
            }
            return "puntos/formulario";
        }
    }

    @PostMapping("/asignar")
    public String asignar(@RequestParam(required = false) Integer idUsuario,
                          @RequestParam(required = false) Integer idPuntoVenta,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                          HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            puntoService.asignar(idUsuario, idPuntoVenta, fecha);
            redireccion.addFlashAttribute("exito", "La asignación se registró correctamente.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/puntos";
    }
}
