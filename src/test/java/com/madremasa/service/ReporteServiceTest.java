package com.madremasa.service;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.dto.VentaProducto;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.DetalleCierreRepositorio;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas del período de los reportes y de la exportación a CSV (RF-24, RF-26)
class ReporteServiceTest {

    private CierreVentaRepositorio cierreRepositorio;
    private DetalleCierreRepositorio detalleRepositorio;
    private ReporteService servicio;

    @BeforeEach
    void preparar() {
        cierreRepositorio = mock(CierreVentaRepositorio.class);
        detalleRepositorio = mock(DetalleCierreRepositorio.class);
        servicio = new ReporteService(cierreRepositorio, detalleRepositorio,
                mock(MovimientoInventarioRepositorio.class), mock(ProduccionRepositorio.class));
    }

    @Test
    void sinFechasSeUsaElMesActualHastaHoy() {
        ReporteService.Periodo periodo = servicio.periodo(null, null);

        assertEquals(LocalDate.now().withDayOfMonth(1), periodo.desde());
        assertEquals(LocalDate.now(), periodo.hasta());
    }

    @Test
    void fechaInicialPosteriorALaFinalSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.periodo(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 1)));
    }

    @Test
    void csvDeVentasUsaPuntoYComaYComaDecimal() {
        LocalDate dia = LocalDate.of(2026, 11, 16);
        when(cierreRepositorio.resumirPorPuntoVenta(dia, dia)).thenReturn(List.of(new ResumenPuntoVenta("Feria A", 1L,
                new BigDecimal("37000"), new BigDecimal("25000"), new BigDecimal("62000"))));
        when(detalleRepositorio.resumirPorProducto(dia, dia)).thenReturn(List.of(new VentaProducto("Focaccia de romero", 12L)));
        when(cierreRepositorio.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(List.of());

        String csv = servicio.csvVentas(servicio.periodo(dia, dia));

        assertTrue(csv.startsWith("﻿"));
        assertTrue(csv.contains("Feria A;1;37000,00;25000,00;62000,00"));
        assertTrue(csv.contains("Total;;;;62000,00"));
        assertTrue(csv.contains("Focaccia de romero;12"));
    }

    @Test
    void campoConPuntoYComaSeEncierraEntreComillas() {
        LocalDate dia = LocalDate.of(2026, 11, 16);
        when(cierreRepositorio.resumirPorPuntoVenta(dia, dia)).thenReturn(List.of());
        when(detalleRepositorio.resumirPorProducto(dia, dia)).thenReturn(List.of(new VentaProducto("Pan; edición especial", 3L)));
        when(cierreRepositorio.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(List.of());

        String csv = servicio.csvVentas(servicio.periodo(dia, dia));

        assertTrue(csv.contains("\"Pan; edición especial\";3"));
    }
}
