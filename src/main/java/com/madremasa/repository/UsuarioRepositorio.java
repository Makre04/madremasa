package com.madremasa.repository;

import com.madremasa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCodigoEmpleado(String codigoEmpleado);

    List<Usuario> findAllByOrderByCodigoEmpleadoAsc();

    boolean existsByCodigoEmpleado(String codigoEmpleado);

    // Empleados activos de un rol, por ejemplo para asignarlos a un punto de venta
    List<Usuario> findByRolNombreAndActivoTrueOrderByCodigoEmpleadoAsc(String nombreRol);
}
