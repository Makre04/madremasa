package com.madremasa.repository;

import com.madremasa.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
