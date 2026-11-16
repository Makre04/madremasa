package com.madremasa.service;

import com.madremasa.model.DetalleReceta;
import com.madremasa.model.Insumo;
import com.madremasa.model.MovimientoInventario;
import com.madremasa.model.Produccion;
import com.madremasa.model.Producto;
import com.madremasa.model.Receta;
import com.madremasa.model.UnidadMedida;
import com.madremasa.model.Usuario;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas de la confirmación de producción (RF-16, RF-18, RN-04, RN-05)
class ProduccionServiceTest {

    private ProduccionRepositorio produccionRepositorio;
    private MovimientoInventarioRepositorio movimientoRepositorio;
    private ProduccionService servicio;
    private Receta focaccia;
    private Insumo harina;
    private Insumo aceite;

    @BeforeEach
    void preparar() {
        RecetaRepositorio recetaRepositorio = mock(RecetaRepositorio.class);
        produccionRepositorio = mock(ProduccionRepositorio.class);
        movimientoRepositorio = mock(MovimientoInventarioRepositorio.class);
        UsuarioRepositorio usuarioRepositorio = mock(UsuarioRepositorio.class);
        RecetaService recetaService = mock(RecetaService.class);
        servicio = new ProduccionService(recetaRepositorio, produccionRepositorio, movimientoRepositorio,
                usuarioRepositorio, recetaService);

        harina = insumo("Harina de trigo", "g", "22500");
        aceite = insumo("Aceite de oliva", "ml", "1000");

        Producto producto = new Producto();
        producto.setNombre("Focaccia de romero");
        focaccia = new Receta();
        focaccia.setProducto(producto);
        focaccia.setRendimiento(8);
        focaccia.getDetalles().add(new DetalleReceta(focaccia, harina, new BigDecimal("3000")));
        focaccia.getDetalles().add(new DetalleReceta(focaccia, aceite, new BigDecimal("150")));

        when(recetaRepositorio.findById(1)).thenReturn(Optional.of(focaccia));
        when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(new Usuario()));
        when(recetaService.costoPorTanda(focaccia)).thenReturn(new BigDecimal("3060.30"));
    }

    private static Insumo insumo(String nombre, String simbolo, String existencia) {
        Insumo insumo = new Insumo();
        insumo.setNombre(nombre);
        insumo.setUnidad(new UnidadMedida(nombre, simbolo));
        insumo.setExistencia(new BigDecimal(existencia));
        return insumo;
    }

    @Test
    void confirmarDescuentaLosInsumosYCalculaUnidadesYCosto() {
        Produccion produccion = servicio.confirmar(1, 2, 1);

        assertEquals(16, produccion.getUnidadesProducidas());
        assertEquals(0, new BigDecimal("6120.60").compareTo(produccion.getCostoTotal()));
        assertEquals(0, new BigDecimal("16500").compareTo(harina.getExistencia()));
        assertEquals(0, new BigDecimal("700").compareTo(aceite.getExistencia()));
        verify(produccionRepositorio).save(any(Produccion.class));
        verify(movimientoRepositorio, times(2)).save(any(MovimientoInventario.class));
    }

    @Test
    void siFaltaUnInsumoNoSeDescuentaNinguno() {
        // 8 tandas necesitan 24 000 g de harina y solo hay 22 500 g
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(1, 8, 1));

        assertEquals(0, new BigDecimal("22500").compareTo(harina.getExistencia()));
        assertEquals(0, new BigDecimal("1000").compareTo(aceite.getExistencia()));
        verify(produccionRepositorio, never()).save(any());
        verify(movimientoRepositorio, never()).save(any());
    }

    @Test
    void simularMuestraElFaltanteSinCambiarExistencias() {
        var necesidades = servicio.simular(1, 8);

        assertEquals(0, new BigDecimal("1500").compareTo(necesidades.get(0).faltante()));
        assertEquals(0, new BigDecimal("22500").compareTo(harina.getExistencia()));
    }

    @Test
    void tandasFueraDeRangoSeRechazan() {
        assertThrows(ReglaNegocioException.class, () -> servicio.confirmar(1, 0, 1));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.confirmar(1, ProduccionService.MAXIMO_TANDAS + 1, 1));
    }
}
