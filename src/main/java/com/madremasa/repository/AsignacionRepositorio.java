package com.madremasa.repository;

import com.madremasa.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsignacionRepositorio extends JpaRepository<Asignacion, Integer> {

    Optional<Asignacion> findByUsuarioIdUsuarioAndFecha(Integer idUsuario, LocalDate fecha);

    boolean existsByUsuarioIdUsuarioAndFecha(Integer idUsuario, LocalDate fecha);

    List<Asignacion> findAllByOrderByFechaDesc();
}
