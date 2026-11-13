package com.madremasa.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Fórmula de un producto: ingredientes por tanda y rendimiento
@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer idReceta;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    // Unidades que produce una tanda
    @Column(name = "rendimiento", nullable = false)
    private Integer rendimiento;

    @Column(name = "instrucciones", length = 1000)
    private String instrucciones;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "receta")
    @OrderBy("cantidad DESC")
    private List<DetalleReceta> detalles = new ArrayList<>();

    public Integer getIdReceta() { return idReceta; }
    public Producto getProducto() { return producto; }
    public Integer getRendimiento() { return rendimiento; }
    public String getInstrucciones() { return instrucciones; }
    public Boolean getActivo() { return activo; }
    public List<DetalleReceta> getDetalles() { return detalles; }
}
