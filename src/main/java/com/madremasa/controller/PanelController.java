package com.madremasa.controller;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.security.core.Authentication;
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

    public PanelController(UsuarioRepositorio usuarioRepositorio, InsumoRepositorio insumoRepositorio,
                           ProduccionRepositorio produccionRepositorio, CierreVentaRepositorio cierreRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.insumoRepositorio = insumoRepositorio;
        this.produccionRepositorio = produccionRepositorio;
        this.cierreRepositorio = cierreRepositorio;
    }

    @GetMapping("/panel")
    public String panel(Authentication autenticacion, Model modelo) {
        usuarioRepositorio.findByCodigoEmpleado(autenticacion.getName())
                .ifPresent(usuario -> modelo.addAttribute("usuario", usuario));
        modelo.addAttribute("insumosBajos", insumoRepositorio.buscarBajos());
        modelo.addAttribute("totalInsumos", insumoRepositorio.count());
        modelo.addAttribute("producciones", produccionRepositorio.findTop5ByOrderByFechaDesc());
        BigDecimal totalVentas = cierreRepositorio.resumirPorPuntoVenta().stream()
                .map(ResumenPuntoVenta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        modelo.addAttribute("totalVentas", totalVentas);
        modelo.addAttribute("misCierres", cierreRepositorio.findByUsuarioCodigoEmpleadoOrderByFechaDesc(autenticacion.getName()));
        return "panel";
    }
}
