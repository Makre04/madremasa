package com.madremasa.service;

import com.madremasa.model.Insumo;
import com.madremasa.model.MovimientoInventario;
import com.madremasa.model.UnidadMedida;
import com.madremasa.model.Usuario;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas de entradas y ajustes de inventario (RF-09, RF-10, RN-03, RN-07, RN-11)
class InventarioServiceTest {

    private InsumoRepositorio insumoRepositorio;
    private MovimientoInventarioRepositorio movimientoRepositorio;
    private InventarioService servicio;
    private Insumo harina;

    @BeforeEach
    void preparar() {
        insumoRepositorio = mock(InsumoRepositorio.class);
        movimientoRepositorio = mock(MovimientoInventarioRepositorio.class);
        UsuarioRepositorio usuarioRepositorio = mock(UsuarioRepositorio.class);
        servicio = new InventarioService(insumoRepositorio, movimientoRepositorio, usuarioRepositorio);

        harina = new Insumo();
        harina.setNombre("Harina de trigo");
        harina.setUnidad(new UnidadMedida("gramo", "g"));
        harina.setExistencia(new BigDecimal("3000"));
        harina.setCostoUnitario(new BigDecimal("0.95"));

        when(insumoRepositorio.findById(1)).thenReturn(Optional.of(harina));
        when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(new Usuario()));
        when(movimientoRepositorio.save(any(MovimientoInventario.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void entradaSumaExistenciaYRecalculaCostoPromedioPonderado() {
        servicio.registrarEntrada(1, new BigDecimal("20000"), new BigDecimal("18000"), 1);

        // (3000 × 0,95 + 18000) / 23000 = 0,9065
        assertEquals(0, new BigDecimal("23000").compareTo(harina.getExistencia()));
        assertEquals(new BigDecimal("0.9065"), harina.getCostoUnitario());
        verify(movimientoRepositorio).save(any(MovimientoInventario.class));
    }

    @Test
    void entradaConCantidadCeroSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarEntrada(1, BigDecimal.ZERO, new BigDecimal("100"), 1));
        verify(movimientoRepositorio, never()).save(any());
    }

    @Test
    void ajusteQueDejaExistenciaNegativaSeRechaza() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAjuste(1, new BigDecimal("-30000"), "Merma", 1));

        assertTrue(error.getMessage().contains("negativo"));
        assertEquals(0, new BigDecimal("3000").compareTo(harina.getExistencia()));
        verify(movimientoRepositorio, never()).save(any());
    }

    @Test
    void ajusteSinMotivoSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAjuste(1, new BigDecimal("-500"), "  ", 1));
    }

    @Test
    void ajusteValidoRestaLaExistencia() {
        MovimientoInventario movimiento = servicio.registrarAjuste(1, new BigDecimal("-500"), "Merma", 1);

        assertEquals(0, new BigDecimal("2500").compareTo(harina.getExistencia()));
        assertEquals("AJUSTE", movimiento.getTipo());
        assertEquals("Merma", movimiento.getMotivo());
    }
}
