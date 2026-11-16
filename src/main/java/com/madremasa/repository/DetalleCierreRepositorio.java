package com.madremasa.repository;

import com.madremasa.dto.VentaProducto;
import com.madremasa.model.DetalleCierre;
import com.madremasa.model.DetalleCierreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;

public interface DetalleCierreRepositorio extends JpaRepository<DetalleCierre, DetalleCierreId> {

    // Unidades vendidas por producto en todos los cierres
    @Query("select new com.madremasa.dto.VentaProducto(d.producto.nombre, sum(d.unidadesVendidas)) "
            + "from DetalleCierre d group by d.producto.nombre order by sum(d.unidadesVendidas) desc")
    List<VentaProducto> resumirPorProducto();

    // Unidades vendidas por producto en los cierres de un período (RF-24)
    @Query("select new com.madremasa.dto.VentaProducto(d.producto.nombre, sum(d.unidadesVendidas)) "
            + "from DetalleCierre d where d.cierre.fecha between :desde and :hasta "
            + "group by d.producto.nombre order by sum(d.unidadesVendidas) desc")
    List<VentaProducto> resumirPorProducto(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
