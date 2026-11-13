package com.madremasa.model;

import jakarta.persistence.*;

// Unidad base en la que se guardan las cantidades (g, ml o u)
@Entity
@Table(name = "unidad_medida")
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "nombre", nullable = false, length = 20)
    private String nombre;

    @Column(name = "simbolo", nullable = false, length = 5)
    private String simbolo;

    public Integer getIdUnidad() { return idUnidad; }
    public String getNombre() { return nombre; }
    public String getSimbolo() { return simbolo; }
}
