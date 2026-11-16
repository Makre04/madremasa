package com.madremasa.service;

import com.madremasa.model.CierreVenta;
import com.madremasa.model.DetalleCierre;
import com.madremasa.model.Producto;
import com.madremasa.model.PuntoVenta;
import com.madremasa.repository.AsignacionRepositorio;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.DetalleCierreRepositorio;
import com.madremasa.repository.ProductoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas del registro y la corrección de cierres (RF-21, RF-22, RN-14, RN-15)
class CierreServiceTest {

    private AsignacionRepositorio asignacionRepositorio;
    private CierreVentaRepositorio cierreRepositorio;
    private DetalleCierreRepositorio detalleRepositorio;
    private ProductoRepositorio productoRepositorio;
    private CierreService servicio;
    private CierreVenta cierre;
    private Producto focaccia;
    private Producto panClasico;

    @BeforeEach
    void preparar() {
        asignacionRepositorio = mock(AsignacionRepositorio.class);
        cierreRepositorio = mock(CierreVentaRepositorio.class);
        detalleRepositorio = mock(DetalleCierreRepositorio.class);
        productoRepositorio = mock(ProductoRepositorio.class);
        servicio = new CierreService(asignacionRepositorio, cierreRepositorio, detalleRepositorio, productoRepositorio);

        focaccia = mock(Producto.class);
        when(focaccia.getIdProducto()).thenReturn(1);
        panClasico = mock(Producto.class);
        when(panClasico.getIdProducto()).thenReturn(2);

        PuntoVenta feria = mock(PuntoVenta.class);
        when(feria.getNombre()).thenReturn("Feria A");
        cierre = new CierreVenta();
        cierre.setPuntoVenta(feria);
        cierre.setMontoEfectivo(new BigDecimal("37000"));
        cierre.setMontoDatafono(new BigDecimal("25000"));
        cierre.getDetalles().add(new DetalleCierre(cierre, focaccia, 12));

        when(cierreRepositorio.findById(10)).thenReturn(Optional.of(cierre));
        when(productoRepositorio.findById(2)).thenReturn(Optional.of(panClasico));
    }

    @Test
    void sinAsignacionParaHoyNoSePuedeRegistrarCierre() {
        when(asignacionRepositorio.findByUsuarioIdUsuarioAndFecha(any(), any())).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(3, new BigDecimal("1000"),
                BigDecimal.ZERO, List.of(1), List.of(1)));
        verify(cierreRepositorio, never()).save(any());
    }

    @Test
    void correccionActualizaMontosYUnidades() {
        servicio.corregir(10, new BigDecimal("40000"), new BigDecimal("25000"), List.of(1, 2), List.of(13, 8));

        assertEquals(0, new BigDecimal("40000").compareTo(cierre.getMontoEfectivo()));
        assertEquals(13, cierre.getDetalles().get(0).getUnidadesVendidas());
        // El pan clásico no estaba en el cierre original y se agrega
        assertEquals(2, cierre.getDetalles().size());
        assertEquals(8, cierre.getDetalles().get(1).getUnidadesVendidas());
    }

    @Test
    void correccionConMontoNegativoSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.corregir(10, new BigDecimal("-1"),
                new BigDecimal("25000"), List.of(1), List.of(12)));

        assertEquals(0, new BigDecimal("37000").compareTo(cierre.getMontoEfectivo()));
        verify(cierreRepositorio, never()).save(any());
    }

    @Test
    void correccionDeCierreInexistenteSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.corregir(99, BigDecimal.ONE, BigDecimal.ONE,
                List.of(1), List.of(1)));
    }
}
