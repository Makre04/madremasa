package com.madremasa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Materia prima de la panadería
@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Integer idInsumo;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unidad")
    private UnidadMedida unidad;

    @Column(name = "existencia", nullable = false, precision = 12, scale = 3)
    private BigDecimal existencia = BigDecimal.ZERO;

    @Column(name = "cantidad_minima", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadMinima = BigDecimal.ZERO;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    // RN-10: el insumo está bajo cuando la existencia es igual o menor que el mínimo
    public boolean isBajo() {
        return existencia.compareTo(cantidadMinima) <= 0;
    }

    public Integer getIdInsumo() { return idInsumo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public UnidadMedida getUnidad() { return unidad; }
    public void setUnidad(UnidadMedida unidad) { this.unidad = unidad; }
    public BigDecimal getExistencia() { return existencia; }
    public BigDecimal getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(BigDecimal cantidadMinima) { this.cantidadMinima = cantidadMinima; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
