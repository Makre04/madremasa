package com.madremasa.service;

import com.madremasa.dto.Necesidad;
import com.madremasa.model.DetalleReceta;
import com.madremasa.model.Insumo;
import com.madremasa.model.Receta;
import com.madremasa.repository.RecetaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Reglas de producción: cálculo de necesidades y faltantes (RF-17, RF-19, RN-04, RN-05)
@Service
public class ProduccionService {

    private final RecetaRepositorio recetaRepositorio;

    public ProduccionService(RecetaRepositorio recetaRepositorio) {
        this.recetaRepositorio = recetaRepositorio;
    }

    // Simula la producción sin modificar el inventario
    @Transactional(readOnly = true)
    public List<Necesidad> simular(Integer idReceta, int tandas) {
        Receta receta = recetaRepositorio.findById(idReceta)
                .orElseThrow(() -> new IllegalArgumentException("La receta no existe"));

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
}
