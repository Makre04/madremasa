package com.madremasa.controller;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

// Panel principal con indicadores según el rol (RF-23)
@Controller
public class PanelController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final InsumoRepositorio insumoRepositorio;
    private final ProduccionRepositorio produccionRepositorio;
    private final CierreVentaRepositorio cierreRepositorio;
    private final ControlAcceso controlAcceso;

    public PanelController(UsuarioRepositorio usuarioRepositorio, InsumoRepositorio insumoRepositorio,
                           ProduccionRepositorio produccionRepositorio, CierreVentaRepositorio cierreRepositorio,
                           ControlAcceso controlAcceso) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.insumoRepositorio = insumoRepositorio;
        this.produccionRepositorio = produccionRepositorio;
        this.cierreRepositorio = cierreRepositorio;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping("/panel")
    public String panel(HttpSession sesion, Model modelo) {
        // Cualquier usuario con sesión iniciada puede ver el panel
        String acceso = controlAcceso.verificar(sesion);
        if (acceso != null) return acceso;

        String codigo = controlAcceso.codigoUsuario(sesion);
        usuarioRepositorio.findByCodigoEmpleado(codigo)
                .ifPresent(usuario -> modelo.addAttribute("usuario", usuario));
        modelo.addAttribute("insumosBajos", insumoRepositorio.buscarBajos());
        modelo.addAttribute("totalInsumos", insumoRepositorio.count());
        modelo.addAttribute("producciones", produccionRepositorio.findTop5ByOrderByFechaDesc());
        BigDecimal totalVentas = cierreRepositorio.resumirPorPuntoVenta().stream()
                .map(ResumenPuntoVenta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        modelo.addAttribute("totalVentas", totalVentas);
        modelo.addAttribute("misCierres", cierreRepositorio.findByUsuarioCodigoEmpleadoOrderByFechaDesc(codigo));
        return "panel";
    }
}
