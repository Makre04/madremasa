package com.madremasa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// Llave compuesta de detalle_receta (receta + insumo)
@Embeddable
public class DetalleRecetaId implements Serializable {

    @Column(name = "id_receta")
    private Integer idReceta;

    @Column(name = "id_insumo")
    private Integer idInsumo;

    public DetalleRecetaId() {
    }

    public Integer getIdReceta() { return idReceta; }
    public Integer getIdInsumo() { return idInsumo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleRecetaId otro)) return false;
        return Objects.equals(idReceta, otro.idReceta) && Objects.equals(idInsumo, otro.idInsumo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReceta, idInsumo);
    }
}
