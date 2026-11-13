package com.madremasa.service;

import com.madremasa.dto.InsumoFormulario;
import com.madremasa.model.Insumo;
import com.madremasa.model.UnidadMedida;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.UnidadMedidaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Registro y edición de insumos (RF-08)
@Service
public class InsumoService {

    private final InsumoRepositorio insumoRepositorio;
    private final UnidadMedidaRepositorio unidadRepositorio;

    public InsumoService(InsumoRepositorio insumoRepositorio, UnidadMedidaRepositorio unidadRepositorio) {
        this.insumoRepositorio = insumoRepositorio;
        this.unidadRepositorio = unidadRepositorio;
    }

    // Indica si ya existe otro insumo con el mismo nombre
    public boolean nombreRepetido(InsumoFormulario formulario) {
        String nombre = formulario.getNombre().trim();
        if (formulario.getIdInsumo() == null) {
            return insumoRepositorio.existsByNombreIgnoreCase(nombre);
        }
        return insumoRepositorio.existsByNombreIgnoreCaseAndIdInsumoNot(nombre, formulario.getIdInsumo());
    }

    // Crea o actualiza el insumo; la existencia solo cambia con movimientos de inventario
    @Transactional
    public Insumo guardar(InsumoFormulario formulario) {
        Insumo insumo = formulario.getIdInsumo() == null
                ? new Insumo()
                : insumoRepositorio.findById(formulario.getIdInsumo())
                    .orElseThrow(() -> new IllegalArgumentException("El insumo no existe"));
        UnidadMedida unidad = unidadRepositorio.findById(formulario.getIdUnidad())
                .orElseThrow(() -> new IllegalArgumentException("La unidad no existe"));

        insumo.setNombre(formulario.getNombre().trim());
        insumo.setUnidad(unidad);
        insumo.setCantidadMinima(formulario.getCantidadMinima());
        insumo.setCostoUnitario(formulario.getCostoUnitario());
        insumo.setActivo(formulario.isActivo());
        return insumoRepositorio.save(insumo);
    }

    // Copia los datos de un insumo existente al formulario de edición
    public InsumoFormulario aFormulario(Insumo insumo) {
        InsumoFormulario formulario = new InsumoFormulario();
        formulario.setIdInsumo(insumo.getIdInsumo());
        formulario.setNombre(insumo.getNombre());
        formulario.setIdUnidad(insumo.getUnidad().getIdUnidad());
        formulario.setCantidadMinima(insumo.getCantidadMinima());
        formulario.setCostoUnitario(insumo.getCostoUnitario());
        formulario.setActivo(insumo.getActivo());
        return formulario;
    }
}
