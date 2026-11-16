package com.madremasa.repository;

import com.madremasa.model.DetalleReceta;
import com.madremasa.model.DetalleRecetaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleRecetaRepositorio extends JpaRepository<DetalleReceta, DetalleRecetaId> {
}
