package com.madremasa.repository;

import com.madremasa.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Integer> {

    // Historial de un insumo, del más reciente al más antiguo (RF-11)
    List<MovimientoInventario> findByInsumoIdInsumoOrderByFechaDescIdMovimientoDesc(Integer idInsumo);
}
