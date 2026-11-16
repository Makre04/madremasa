package com.madremasa.service;

import com.madremasa.dto.ConsumoInsumo;
import com.madremasa.dto.CostoProduccion;
import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.dto.VentaProducto;
import com.madremasa.model.CierreVenta;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.DetalleCierreRepositorio;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Reportes por período (RF-24, RF-25) y su exportación a CSV (RF-26)
@Service
public class ReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CierreVentaRepositorio cierreRepositorio;
    private final DetalleCierreRepositorio detalleCierreRepositorio;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final ProduccionRepositorio produccionRepositorio;

    public ReporteService(CierreVentaRepositorio cierreRepositorio, DetalleCierreRepositorio detalleCierreRepositorio,
                          MovimientoInventarioRepositorio movimientoRepositorio, ProduccionRepositorio produccionRepositorio) {
        this.cierreRepositorio = cierreRepositorio;
        this.detalleCierreRepositorio = detalleCierreRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.produccionRepositorio = produccionRepositorio;
    }

    // Período del reporte. Si no se indican fechas, se usa el mes actual hasta hoy.
    public record Periodo(LocalDate desde, LocalDate hasta) {
        public String texto() {
            return desde.format(FECHA) + " al " + hasta.format(FECHA);
        }
    }

    public Periodo periodo(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hasta != null ? hasta : hoy;
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return new Periodo(inicio, fin);
    }

    // ----- Ventas (RF-24) -----

    public List<ResumenPuntoVenta> ventasPorPunto(Periodo p) {
        return cierreRepositorio.resumirPorPuntoVenta(p.desde(), p.hasta());
    }

    public List<VentaProducto> ventasPorProducto(Periodo p) {
        return detalleCierreRepositorio.resumirPorProducto(p.desde(), p.hasta());
    }

    public List<CierreVenta> cierres(Periodo p) {
        return cierreRepositorio.findByFechaBetweenOrderByFechaDesc(p.desde(), p.hasta());
    }

    public BigDecimal totalVentas(List<ResumenPuntoVenta> resumen) {
        return resumen.stream().map(ResumenPuntoVenta::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ----- Consumo de insumos y costo de producción (RF-25) -----

    // Las producciones guardan fecha y hora; el día final se incluye completo
    public List<ConsumoInsumo> consumoPorInsumo(Periodo p) {
        return movimientoRepositorio.consumoPorInsumo(p.desde().atStartOfDay(), p.hasta().plusDays(1).atStartOfDay());
    }

    public List<CostoProduccion> costoPorProducto(Periodo p) {
        return produccionRepositorio.resumirPorProducto(p.desde().atStartOfDay(), p.hasta().plusDays(1).atStartOfDay());
    }

    public BigDecimal totalCostoProduccion(List<CostoProduccion> filas) {
        return filas.stream().map(CostoProduccion::costo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ----- Exportación a CSV (RF-26) -----
    // Se usa punto y coma como separador, coma decimal y BOM de UTF-8 para que Excel en español abra bien las columnas y las tildes.

    public String csvVentas(Periodo p) {
        StringBuilder csv = new StringBuilder("﻿");
        linea(csv, "Reporte de ventas", p.texto());
        linea(csv);
        linea(csv, "Punto de venta", "Cierres", "Efectivo", "Datafono", "Total");
        List<ResumenPuntoVenta> resumen = ventasPorPunto(p);
        for (ResumenPuntoVenta r : resumen) {
            linea(csv, r.puntoVenta(), r.cierres().toString(), monto(r.efectivo()), monto(r.datafono()), monto(r.total()));
        }
        linea(csv, "Total", "", "", "", monto(totalVentas(resumen)));
        linea(csv);
        linea(csv, "Producto", "Unidades vendidas");
        for (VentaProducto v : ventasPorProducto(p)) {
            linea(csv, v.producto(), v.unidades().toString());
        }
        linea(csv);
        linea(csv, "Fecha", "Punto de venta", "Registrado por", "Efectivo", "Datafono", "Total");
        for (CierreVenta c : cierres(p)) {
            linea(csv, c.getFecha().format(FECHA), c.getPuntoVenta().getNombre(), c.getUsuario().getCodigoEmpleado(),
                    monto(c.getMontoEfectivo()), monto(c.getMontoDatafono()), monto(c.getTotal()));
        }
        return csv.toString();
    }

    public String csvConsumo(Periodo p) {
        StringBuilder csv = new StringBuilder("﻿");
        linea(csv, "Reporte de consumo de insumos y costo de produccion", p.texto());
        linea(csv);
        linea(csv, "Insumo", "Unidad", "Cantidad consumida", "Costo al costo actual");
        for (ConsumoInsumo c : consumoPorInsumo(p)) {
            linea(csv, c.insumo(), c.unidad(), c.cantidad().stripTrailingZeros().toPlainString().replace('.', ','), monto(c.costo()));
        }
        linea(csv);
        linea(csv, "Producto", "Producciones", "Tandas", "Unidades", "Costo de produccion");
        List<CostoProduccion> filas = costoPorProducto(p);
        for (CostoProduccion f : filas) {
            linea(csv, f.producto(), f.producciones().toString(), f.tandas().toString(), f.unidades().toString(),
                    monto(f.costo()));
        }
        linea(csv, "Total", "", "", "", monto(totalCostoProduccion(filas)));
        return csv.toString();
    }

    private static String monto(BigDecimal valor) {
        // Coma decimal, como la usa Excel con la configuración regional de Costa Rica
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    // Escapa los campos que traen separador, comillas o saltos de línea
    private static void linea(StringBuilder csv, String... campos) {
        for (int i = 0; i < campos.length; i++) {
            String campo = campos[i] == null ? "" : campos[i];
            if (campo.contains(";") || campo.contains("\"") || campo.contains("\n")) {
                campo = "\"" + campo.replace("\"", "\"\"") + "\"";
            }
            if (i > 0) csv.append(';');
            csv.append(campo);
        }
        csv.append("\r\n");
    }
}
