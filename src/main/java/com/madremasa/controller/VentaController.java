package com.madremasa.controller;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.service.ReglaNegocioException;
import com.madremasa.service.ReporteService;
import com.madremasa.sesion.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

// Reportes por período para el administrador: ventas por punto de venta (RF-24),
// consumo de insumos y costo de producción (RF-25) y su exportación a CSV (RF-26).
@Controller
public class VentaController {

    private final ReporteService reporteService;
    private final ControlAcceso controlAcceso;

    public VentaController(ReporteService reporteService, ControlAcceso controlAcceso) {
        this.reporteService = reporteService;
        this.controlAcceso = controlAcceso;
    }

    @GetMapping("/ventas")
    public String ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        ReporteService.Periodo periodo = periodoSeguro(desde, hasta, modelo);
        List<ResumenPuntoVenta> resumen = reporteService.ventasPorPunto(periodo);
        modelo.addAttribute("periodo", periodo);
        modelo.addAttribute("resumen", resumen);
        modelo.addAttribute("totalGeneral", reporteService.totalVentas(resumen));
        modelo.addAttribute("productos", reporteService.ventasPorProducto(periodo));
        modelo.addAttribute("cierres", reporteService.cierres(periodo));
        return "ventas/reporte";
    }

    @GetMapping("/ventas/csv")
    public ResponseEntity<byte[]> ventasCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                            HttpSession sesion) {
        if (controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR) != null) {
            return ResponseEntity.status(403).build();
        }
        ReporteService.Periodo periodo = reporteService.periodo(desde, hasta);
        return archivoCsv("ventas_" + periodo.desde() + "_" + periodo.hasta() + ".csv", reporteService.csvVentas(periodo));
    }

    @GetMapping("/reportes/consumo")
    public String consumo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          HttpSession sesion, Model modelo) {
        String acceso = controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR);
        if (acceso != null) return acceso;

        ReporteService.Periodo periodo = periodoSeguro(desde, hasta, modelo);
        var produccion = reporteService.costoPorProducto(periodo);
        modelo.addAttribute("periodo", periodo);
        modelo.addAttribute("consumo", reporteService.consumoPorInsumo(periodo));
        modelo.addAttribute("produccion", produccion);
        modelo.addAttribute("costoTotal", reporteService.totalCostoProduccion(produccion));
        return "reportes/consumo";
    }

    @GetMapping("/reportes/consumo/csv")
    public ResponseEntity<byte[]> consumoCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             HttpSession sesion) {
        if (controlAcceso.verificar(sesion, ControlAcceso.ADMINISTRADOR) != null) {
            return ResponseEntity.status(403).build();
        }
        ReporteService.Periodo periodo = reporteService.periodo(desde, hasta);
        return archivoCsv("consumo_" + periodo.desde() + "_" + periodo.hasta() + ".csv", reporteService.csvConsumo(periodo));
    }

    // Si el período es inválido se muestra el mensaje y se usa el mes actual
    private ReporteService.Periodo periodoSeguro(LocalDate desde, LocalDate hasta, Model modelo) {
        try {
            return reporteService.periodo(desde, hasta);
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            return reporteService.periodo(null, null);
        }
    }

    private ResponseEntity<byte[]> archivoCsv(String nombre, String contenido) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenido.getBytes(StandardCharsets.UTF_8));
    }
}
