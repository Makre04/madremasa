package com.madremasa.repository;

import com.madremasa.model.Produccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduccionRepositorio extends JpaRepository<Produccion, Integer> {

    List<Produccion> findTop5ByOrderByFechaDesc();
}
