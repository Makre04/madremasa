package com.madremasa.model;

import jakarta.persistence.*;

// Unidades vendidas de un producto en un cierre
@Entity
@Table(name = "detalle_cierre")
public class DetalleCierre {

    @EmbeddedId
    private DetalleCierreId id;

    @ManyToOne(optional = false)
    @MapsId("idCierre")
    @JoinColumn(name = "id_cierre")
    private CierreVenta cierre;

    @ManyToOne(optional = false)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "unidades_vendidas", nullable = false)
    private Integer unidadesVendidas;

    public CierreVenta getCierre() { return cierre; }
    public Producto getProducto() { return producto; }
    public Integer getUnidadesVendidas() { return unidadesVendidas; }
}
