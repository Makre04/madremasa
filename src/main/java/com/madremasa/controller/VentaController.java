package com.madremasa.controller;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.DetalleCierreRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

// Reporte de ventas por punto de venta (RF-24)
@Controller
public class VentaController {

    private final CierreVentaRepositorio cierreRepositorio;
    private final DetalleCierreRepositorio detalleRepositorio;

    public VentaController(CierreVentaRepositorio cierreRepositorio, DetalleCierreRepositorio detalleRepositorio) {
        this.cierreRepositorio = cierreRepositorio;
        this.detalleRepositorio = detalleRepositorio;
    }

    @GetMapping("/ventas")
    public String reporte(Model modelo) {
        List<ResumenPuntoVenta> resumen = cierreRepositorio.resumirPorPuntoVenta();
        BigDecimal totalGeneral = resumen.stream()
                .map(ResumenPuntoVenta::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        modelo.addAttribute("resumen", resumen);
        modelo.addAttribute("totalGeneral", totalGeneral);
        modelo.addAttribute("productos", detalleRepositorio.resumirPorProducto());
        modelo.addAttribute("cierres", cierreRepositorio.findAllByOrderByFechaDesc());
        return "ventas/reporte";
    }
}
