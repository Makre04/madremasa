package com.madremasa.repository;

import com.madremasa.dto.VentaProducto;
import com.madremasa.model.DetalleCierre;
import com.madremasa.model.DetalleCierreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleCierreRepositorio extends JpaRepository<DetalleCierre, DetalleCierreId> {

    // Unidades vendidas por producto en todos los cierres
    @Query("select new com.madremasa.dto.VentaProducto(d.producto.nombre, sum(d.unidadesVendidas)) "
            + "from DetalleCierre d group by d.producto.nombre order by sum(d.unidadesVendidas) desc")
    List<VentaProducto> resumirPorProducto();
}
