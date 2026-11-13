package com.madremasa.repository;

import com.madremasa.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecetaRepositorio extends JpaRepository<Receta, Integer> {

    // Recetas con producto e ingredientes en una sola consulta
    @Query("select distinct r from Receta r join fetch r.producto p "
            + "left join fetch r.detalles d left join fetch d.insumo order by p.nombre")
    List<Receta> buscarTodasConDetalles();
}
