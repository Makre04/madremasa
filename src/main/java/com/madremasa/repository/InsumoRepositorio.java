package com.madremasa.repository;

import com.madremasa.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InsumoRepositorio extends JpaRepository<Insumo, Integer> {

    List<Insumo> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdInsumoNot(String nombre, Integer idInsumo);

    // Insumos activos con existencia igual o menor que el mínimo (RN-10)
    @Query("select i from Insumo i where i.activo = true and i.existencia <= i.cantidadMinima order by i.nombre")
    List<Insumo> buscarBajos();
}
