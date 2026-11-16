package com.madremasa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Ingrediente de una receta con su cantidad por tanda
@Entity
@Table(name = "detalle_receta")
public class DetalleReceta {

    @EmbeddedId
    private DetalleRecetaId id;

    @ManyToOne(optional = false)
    @MapsId("idReceta")
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @ManyToOne(optional = false)
    @MapsId("idInsumo")
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    public DetalleRecetaId getId() { return id; }
    public Receta getReceta() { return receta; }
    public Insumo getInsumo() { return insumo; }
    public BigDecimal getCantidad() { return cantidad; }

    // Costo del ingrediente en una tanda según el costo vigente del insumo
    public BigDecimal getCosto() {
        return cantidad.multiply(insumo.getCostoUnitario());
    }

    public DetalleReceta() {
    }

    // Ingrediente nuevo: la llave compuesta se arma con la receta y el insumo
    public DetalleReceta(Receta receta, Insumo insumo, BigDecimal cantidad) {
        this.id = new DetalleRecetaId(receta.getIdReceta(), insumo.getIdInsumo());
        this.receta = receta;
        this.insumo = insumo;
        this.cantidad = cantidad;
    }

    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
