package com.madremasa.repository;

import com.madremasa.model.MovimientoInventario;
import com.madremasa.dto.ConsumoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Integer> {

    // Historial de un insumo, del más reciente al más antiguo (RF-11)
    List<MovimientoInventario> findByInsumoIdInsumoOrderByFechaDescIdMovimientoDesc(Integer idInsumo);

    // Consumo de cada insumo por producción en un período (RF-25).
    // Los movimientos de producción son negativos, por eso se cambia el signo.
    @Query("select new com.madremasa.dto.ConsumoInsumo(m.insumo.nombre, m.insumo.unidad.simbolo, "
            + "-sum(m.cantidad), -sum(m.cantidad * m.insumo.costoUnitario)) "
            + "from MovimientoInventario m where m.tipo = 'PRODUCCION' and m.fecha >= :desde and m.fecha < :hasta "
            + "group by m.insumo.nombre, m.insumo.unidad.simbolo order by m.insumo.nombre")
    List<ConsumoInsumo> consumoPorInsumo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
