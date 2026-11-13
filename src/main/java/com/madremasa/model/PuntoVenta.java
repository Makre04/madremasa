package com.madremasa.model;

import jakarta.persistence.*;

// Centro de producción o feria del agricultor
@Entity
@Table(name = "punto_venta")
public class PuntoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_punto_venta")
    private Integer idPuntoVenta;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    public Integer getIdPuntoVenta() { return idPuntoVenta; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getUbicacion() { return ubicacion; }
    public Boolean getActivo() { return activo; }
}
