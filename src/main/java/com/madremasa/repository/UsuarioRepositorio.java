package com.madremasa.repository;

import com.madremasa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCodigoEmpleado(String codigoEmpleado);

    List<Usuario> findAllByOrderByCodigoEmpleadoAsc();
}
