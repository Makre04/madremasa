package com.madremasa.repository;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.model.CierreVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CierreVentaRepositorio extends JpaRepository<CierreVenta, Integer> {

    List<CierreVenta> findAllByOrderByFechaDesc();

    List<CierreVenta> findByUsuarioCodigoEmpleadoOrderByFechaDesc(String codigoEmpleado);

    // Totales por punto de venta para el reporte de ventas (RF-24)
    @Query("select new com.madremasa.dto.ResumenPuntoVenta(c.puntoVenta.nombre, count(c), "
            + "sum(c.montoEfectivo), sum(c.montoDatafono), sum(c.total)) "
            + "from CierreVenta c group by c.puntoVenta.nombre order by c.puntoVenta.nombre")
    List<ResumenPuntoVenta> resumirPorPuntoVenta();
}
