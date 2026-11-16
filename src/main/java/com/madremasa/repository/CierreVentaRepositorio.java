package com.madremasa.repository;

import com.madremasa.dto.ResumenPuntoVenta;
import com.madremasa.model.CierreVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CierreVentaRepositorio extends JpaRepository<CierreVenta, Integer> {

    List<CierreVenta> findAllByOrderByFechaDesc();

    List<CierreVenta> findByUsuarioCodigoEmpleadoOrderByFechaDesc(String codigoEmpleado);

    // RN-15: un solo cierre por punto de venta y fecha
    boolean existsByPuntoVentaIdPuntoVentaAndFecha(Integer idPuntoVenta, LocalDate fecha);

    // Totales por punto de venta para el reporte de ventas (RF-24)
    @Query("select new com.madremasa.dto.ResumenPuntoVenta(c.puntoVenta.nombre, count(c), "
            + "sum(c.montoEfectivo), sum(c.montoDatafono), sum(c.total)) "
            + "from CierreVenta c group by c.puntoVenta.nombre order by c.puntoVenta.nombre")
    List<ResumenPuntoVenta> resumirPorPuntoVenta();

    // Cierres de un período, del más reciente al más antiguo (RF-24)
    List<CierreVenta> findByFechaBetweenOrderByFechaDesc(LocalDate desde, LocalDate hasta);

    // Totales por punto de venta en un período (RF-24)
    @Query("select new com.madremasa.dto.ResumenPuntoVenta(c.puntoVenta.nombre, count(c), "
            + "sum(c.montoEfectivo), sum(c.montoDatafono), sum(c.total)) "
            + "from CierreVenta c where c.fecha between :desde and :hasta "
            + "group by c.puntoVenta.nombre order by c.puntoVenta.nombre")
    List<ResumenPuntoVenta> resumirPorPuntoVenta(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
