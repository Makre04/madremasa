package com.madremasa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// Llave compuesta de detalle_cierre (cierre + producto)
@Embeddable
public class DetalleCierreId implements Serializable {

    @Column(name = "id_cierre")
    private Integer idCierre;

    @Column(name = "id_producto")
    private Integer idProducto;

    public DetalleCierreId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleCierreId otro)) return false;
        return Objects.equals(idCierre, otro.idCierre) && Objects.equals(idProducto, otro.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCierre, idProducto);
    }
}
