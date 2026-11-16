package com.madremasa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Producto que se vende en los puntos de venta
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public Integer getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public Boolean getActivo() { return activo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
