package com.madremasa.controller;

import com.madremasa.dto.InsumoFormulario;
import com.madremasa.model.Insumo;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.UnidadMedidaRepositorio;
import com.madremasa.service.InsumoService;
import com.madremasa.service.InventarioService;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

// Insumos e inventario: consulta, registro, edición, eliminación, entradas, ajustes e historial
// (RF-08 a RF-12). Producción consulta y registra entradas; lo demás es exclusivo del administrador.
@Controller
@RequestMapping("/insumos")
public class InsumoController {

    private final InsumoRepositorio insumoRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;
    private final InsumoService insumoService;
    private final InventarioService inventarioService;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final ControlAcceso controlAcceso;

    public InsumoController(InsumoRepositorio insumoRepositorio, UnidadMedidaRepositorio unidadRepositorio,
                            InsumoService insumoService, InventarioService inventarioService,
                            MovimientoInventarioRepositorio movimientoRepositorio, ControlAcceso controlAcceso) {
        this.insumoRepositorio = insumoRepositorio;
        this.unidadRepositorio = unidadRepositorio;
        this.insumoService = insumoService;
        this.inventarioService = inventarioService;
        this.movimientoRepositorio = movimientoRepositorio;
        this.controlAcceso = controlAcceso;
    }

    // CONSULTAR
    @GetMapping
    public String lista(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        modelo.addAttribute("insumos", insumoRepositorio.findAllByOrderByNombreAsc());
        return "insumos/lista";
    }

    // REGISTRAR: muestra el formulario vacío
    @GetMapping("/nuevo")
    public String nuevo(HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        modelo.addAttribute("insumo", new InsumoFormulario());
        modelo.addAttribute("unidades", unidadRepositorio.findAll());
        return "insumos/formulario";
    }

    // MODIFICAR: carga el insumo en el formulario
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, HttpSession sesion, Model modelo,
                         RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        Insumo insumo = insumoRepositorio.findById(id).orElse(null);
        if (insumo == null) {
            redireccion.addFlashAttribute("error", "El insumo solicitado no existe.");
            return "redirect:/insumos";
        }
        modelo.addAttribute("insumo", insumoService.aFormulario(insumo));
        modelo.addAttribute("unidades", unidadRepositorio.findAll());
        return "insumos/formulario";
    }

    // GUARDAR: registra un insumo nuevo o actualiza uno existente
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("insumo") InsumoFormulario formulario, BindingResult errores,
                          HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

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

    // ELIMINAR: un insumo usado en recetas o movimientos no se puede borrar (llave foránea); se debe desactivar
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        Insumo insumo = insumoRepositorio.findById(id).orElse(null);
        if (insumo == null) {
            redireccion.addFlashAttribute("error", "El insumo solicitado no existe.");
            return "redirect:/insumos";
        }
        try {
            insumoService.eliminar(id);
            redireccion.addFlashAttribute("exito", "El insumo \"" + insumo.getNombre() + "\" se eliminó correctamente.");
        } catch (DataIntegrityViolationException e) {
            redireccion.addFlashAttribute("error", "El insumo \"" + insumo.getNombre()
                    + "\" se usa en recetas o movimientos de inventario. En lugar de eliminarlo, márquelo como inactivo.");
        }
        return "redirect:/insumos";
    }

    // HISTORIAL: existencia actual y movimientos del insumo (RF-11)
    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        Insumo insumo = insumoRepositorio.findById(id).orElse(null);
        if (insumo == null) {
            redireccion.addFlashAttribute("error", "El insumo solicitado no existe.");
            return "redirect:/insumos";
        }
        modelo.addAttribute("insumo", insumo);
        modelo.addAttribute("movimientos", movimientoRepositorio.findByInsumoIdInsumoOrderByFechaDescIdMovimientoDesc(id));
        return "insumos/detalle";
    }

    // ENTRADA por compra (RF-09): administrador y producción
    @PostMapping("/{id}/entrada")
    public String entrada(@PathVariable Integer id, @RequestParam(required = false) BigDecimal cantidad,
                          @RequestParam(required = false) BigDecimal costoTotal,
                          HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR, ControlAcceso.PRODUCCION);
        if (acceso != null) return acceso;

        try {
            inventarioService.registrarEntrada(id, cantidad, costoTotal, controlAcceso.idUsuario(sesion));
            redireccion.addFlashAttribute("exito", "La entrada de inventario se registró y la existencia se actualizó.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/insumos/" + id;
    }

    // AJUSTE por merma, pérdida o corrección (RF-10): solo el administrador
    @PostMapping("/{id}/ajuste")
    public String ajuste(@PathVariable Integer id, @RequestParam(required = false) BigDecimal cantidad,
                         @RequestParam(required = false) String motivo,
                         HttpSession sesion, RedirectAttributes redireccion) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        try {
            inventarioService.registrarAjuste(id, cantidad, motivo, controlAcceso.idUsuario(sesion));
            redireccion.addFlashAttribute("exito", "El ajuste se registró correctamente.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/insumos/" + id;
    }
}
