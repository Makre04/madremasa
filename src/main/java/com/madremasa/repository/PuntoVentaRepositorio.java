package com.madremasa.repository;

import com.madremasa.model.PuntoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuntoVentaRepositorio extends JpaRepository<PuntoVenta, Integer> {

    List<PuntoVenta> findAllByOrderByNombreAsc();

    List<PuntoVenta> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdPuntoVentaNot(String nombre, Integer idPuntoVenta);
}
