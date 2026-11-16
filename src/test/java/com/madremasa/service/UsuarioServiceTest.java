package com.madremasa.service;

import com.madremasa.model.Usuario;
import com.madremasa.repository.RolRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pruebas de empleados y del bloqueo por intentos fallidos (RN-01, RNF-01, RNF-03)
class UsuarioServiceTest {

    private UsuarioRepositorio usuarioRepositorio;
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        usuarioRepositorio = mock(UsuarioRepositorio.class);
        servicio = new UsuarioService(usuarioRepositorio, mock(RolRepositorio.class));
    }

    private static Usuario usuarioSinIntentos() {
        Usuario usuario = new Usuario();
        usuario.setIntentosFallidos(0);
        return usuario;
    }

    @Test
    void cuatroIntentosFallidosNoBloquean() {
        Usuario usuario = usuarioSinIntentos();
        for (int i = 0; i < 4; i++) {
            servicio.registrarIntentoFallido(usuario);
        }
        assertEquals(4, usuario.getIntentosFallidos());
        assertFalse(servicio.estaBloqueado(usuario));
    }

    @Test
    void elQuintoIntentoFallidoBloqueaLaCuenta() {
        Usuario usuario = usuarioSinIntentos();
        for (int i = 0; i < UsuarioService.MAXIMO_INTENTOS; i++) {
            servicio.registrarIntentoFallido(usuario);
        }
        assertTrue(servicio.estaBloqueado(usuario));
        verify(usuarioRepositorio, times(5)).save(usuario);
    }

    @Test
    void ingresoCorrectoReiniciaElContadorYElBloqueo() {
        Usuario usuario = usuarioSinIntentos();
        for (int i = 0; i < UsuarioService.MAXIMO_INTENTOS; i++) {
            servicio.registrarIntentoFallido(usuario);
        }
        servicio.registrarIngresoCorrecto(usuario);

        assertFalse(servicio.estaBloqueado(usuario));
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    void codigoConFormatoInvalidoSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("E-5", "Ana Prueba", 2, "Prueba2026!"));
        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void codigoRepetidoSeRechaza() {
        when(usuarioRepositorio.existsByCodigoEmpleado("EMP-002")).thenReturn(true);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear("emp-002", "Ana Prueba", 2, "Prueba2026!"));
        assertTrue(error.getMessage().contains("EMP-002"));
    }

    @Test
    void contrasenaCortaSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear("EMP-009", "Ana Prueba", 2, "corta"));
    }
}
