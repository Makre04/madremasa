package com.madremasa.repository;

import com.madremasa.model.Produccion;
import com.madremasa.dto.CostoProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface ProduccionRepositorio extends JpaRepository<Produccion, Integer> {

    List<Produccion> findTop5ByOrderByFechaDesc();

    // Historial completo de producción (RF-20)
    List<Produccion> findAllByOrderByFechaDesc();

    // Producción confirmada por producto en un período con su costo (RF-25)
    @Query("select new com.madremasa.dto.CostoProduccion(p.receta.producto.nombre, count(p), sum(p.tandas), "
            + "sum(p.unidadesProducidas), sum(p.costoTotal)) "
            + "from Produccion p where p.estado = 'CONFIRMADA' and p.fecha >= :desde and p.fecha < :hasta "
            + "group by p.receta.producto.nombre order by p.receta.producto.nombre")
    List<CostoProduccion> resumirPorProducto(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
