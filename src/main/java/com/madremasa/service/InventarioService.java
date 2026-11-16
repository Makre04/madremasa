package com.madremasa.service;

import com.madremasa.model.Insumo;
import com.madremasa.model.MovimientoInventario;
import com.madremasa.model.Usuario;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Entradas y ajustes de inventario (RF-09, RF-10). Cada operación es transaccional (RNF-10).
@Service
public class InventarioService {

    private final InsumoRepositorio insumoRepositorio;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public InventarioService(InsumoRepositorio insumoRepositorio, MovimientoInventarioRepositorio movimientoRepositorio,
                             UsuarioRepositorio usuarioRepositorio) {
        this.insumoRepositorio = insumoRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // Registra una compra: suma la cantidad y recalcula el costo unitario con promedio ponderado (RN-07)
    @Transactional
    public MovimientoInventario registrarEntrada(Integer idInsumo, BigDecimal cantidad, BigDecimal costoTotal,
                                                 Integer idUsuario) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ReglaNegocioException("La cantidad de la entrada debe ser mayor que cero.");
        }
        if (costoTotal == null || costoTotal.signum() < 0) {
            throw new ReglaNegocioException("El costo total no puede estar vacío ni ser negativo.");
        }
        Insumo insumo = buscarInsumo(idInsumo);

        BigDecimal valorAnterior = insumo.getExistencia().multiply(insumo.getCostoUnitario());
        BigDecimal existenciaNueva = insumo.getExistencia().add(cantidad);
        BigDecimal costoNuevo = valorAnterior.add(costoTotal).divide(existenciaNueva, 4, RoundingMode.HALF_UP);
        insumo.setExistencia(existenciaNueva);
        insumo.setCostoUnitario(costoNuevo);

        MovimientoInventario movimiento = new MovimientoInventario(insumo, "ENTRADA", cantidad, buscarUsuario(idUsuario));
        movimiento.setCostoTotal(costoTotal);
        return movimientoRepositorio.save(movimiento);
    }

    // Ajuste por merma, pérdida o corrección: requiere motivo (RN-11) y no deja existencias negativas (RN-03)
    @Transactional
    public MovimientoInventario registrarAjuste(Integer idInsumo, BigDecimal cantidad, String motivo, Integer idUsuario) {
        if (cantidad == null || cantidad.signum() == 0) {
            throw new ReglaNegocioException("Indique una cantidad distinta de cero (negativa para restar).");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("Todo ajuste de inventario requiere un motivo.");
        }
        Insumo insumo = buscarInsumo(idInsumo);
        BigDecimal existenciaNueva = insumo.getExistencia().add(cantidad);
        if (existenciaNueva.signum() < 0) {
            throw new ReglaNegocioException("El ajuste dejaría la existencia de \"" + insumo.getNombre()
                    + "\" en negativo. Existencia actual: " + insumo.getExistencia().stripTrailingZeros().toPlainString()
                    + " " + insumo.getUnidad().getSimbolo() + ".");
        }
        insumo.setExistencia(existenciaNueva);

        MovimientoInventario movimiento = new MovimientoInventario(insumo, "AJUSTE", cantidad, buscarUsuario(idUsuario));
        movimiento.setMotivo(motivo.trim());
        return movimientoRepositorio.save(movimiento);
    }

    private Insumo buscarInsumo(Integer idInsumo) {
        return insumoRepositorio.findById(idInsumo)
                .orElseThrow(() -> new ReglaNegocioException("El insumo solicitado no existe."));
    }

    private Usuario buscarUsuario(Integer idUsuario) {
        return usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("El usuario de la sesión no existe."));
    }
}
