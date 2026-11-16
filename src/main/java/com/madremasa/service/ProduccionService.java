package com.madremasa.service;

import com.madremasa.dto.Necesidad;
import com.madremasa.model.DetalleReceta;
import com.madremasa.model.Insumo;
import com.madremasa.model.MovimientoInventario;
import com.madremasa.model.Produccion;
import com.madremasa.model.Receta;
import com.madremasa.model.Usuario;
import com.madremasa.repository.MovimientoInventarioRepositorio;
import com.madremasa.repository.ProduccionRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Reglas de producción: necesidades, faltantes y descuento de insumos (RF-16 a RF-19, RN-04, RN-05)
@Service
public class ProduccionService {

    public static final int MAXIMO_TANDAS = 50;

    private final RecetaRepositorio recetaRepositorio;
    private final ProduccionRepositorio produccionRepositorio;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RecetaService recetaService;

    public ProduccionService(RecetaRepositorio recetaRepositorio, ProduccionRepositorio produccionRepositorio,
                             MovimientoInventarioRepositorio movimientoRepositorio,
                             UsuarioRepositorio usuarioRepositorio, RecetaService recetaService) {
        this.recetaRepositorio = recetaRepositorio;
        this.produccionRepositorio = produccionRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.recetaService = recetaService;
    }

    // Simula la producción sin modificar el inventario
    @Transactional(readOnly = true)
    public List<Necesidad> simular(Integer idReceta, int tandas) {
        Receta receta = buscarReceta(idReceta);
        List<Necesidad> necesidades = new ArrayList<>();
        for (DetalleReceta detalle : receta.getDetalles()) {
            Insumo insumo = detalle.getInsumo();
            // RN-05: cantidad de la receta por el número de tandas
            BigDecimal requerido = detalle.getCantidad().multiply(BigDecimal.valueOf(tandas));
            BigDecimal disponible = insumo.getExistencia();
            BigDecimal faltante = requerido.subtract(disponible).max(BigDecimal.ZERO);
            necesidades.add(new Necesidad(insumo.getNombre(), insumo.getUnidad().getSimbolo(),
                    requerido, disponible, faltante));
        }
        return necesidades;
    }

    // Confirma la producción: descuenta los insumos y registra los movimientos.
    // RN-04: si falta un solo insumo no se descuenta ninguno. Todo ocurre en una transacción (RNF-10).
    @Transactional
    public Produccion confirmar(Integer idReceta, int tandas, Integer idUsuario) {
        if (tandas < 1 || tandas > MAXIMO_TANDAS) {
            throw new ReglaNegocioException("Indique entre 1 y " + MAXIMO_TANDAS + " tandas.");
        }
        Receta receta = buscarReceta(idReceta);
        boolean alcanza = simular(idReceta, tandas).stream().allMatch(Necesidad::alcanza);
        if (!alcanza) {
            throw new ReglaNegocioException("No hay insumos suficientes para producir " + tandas
                    + " tanda(s) de " + receta.getProducto().getNombre() + ". No se descontó ningún insumo.");
        }
        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("El usuario de la sesión no existe."));

        Produccion produccion = new Produccion();
        produccion.setReceta(receta);
        produccion.setTandas(tandas);
        produccion.setUnidadesProducidas(receta.getRendimiento() * tandas);
        produccion.setCostoTotal(recetaService.costoPorTanda(receta).multiply(BigDecimal.valueOf(tandas)));
        produccion.setUsuario(usuario);
        produccion.setFecha(LocalDateTime.now());
        produccion.setEstado("CONFIRMADA");
        produccionRepositorio.save(produccion);

        for (DetalleReceta detalle : receta.getDetalles()) {
            Insumo insumo = detalle.getInsumo();
            BigDecimal consumo = detalle.getCantidad().multiply(BigDecimal.valueOf(tandas));
            insumo.setExistencia(insumo.getExistencia().subtract(consumo));

            MovimientoInventario movimiento = new MovimientoInventario(insumo, "PRODUCCION", consumo.negate(), usuario);
            movimiento.setProduccion(produccion);
            movimientoRepositorio.save(movimiento);
        }
        return produccion;
    }

    private Receta buscarReceta(Integer idReceta) {
        if (idReceta == null) {
            throw new ReglaNegocioException("Seleccione una receta.");
        }
        return recetaRepositorio.findById(idReceta)
                .orElseThrow(() -> new ReglaNegocioException("La receta seleccionada no existe."));
    }
}
