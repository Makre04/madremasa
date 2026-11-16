package com.madremasa.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// Punto de venta en el que trabaja un empleado en una fecha (RF-07)
@Entity
@Table(name = "asignacion")
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_punto_venta")
    private PuntoVenta puntoVenta;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    public Asignacion() {
    }

    public Asignacion(Usuario usuario, PuntoVenta puntoVenta, LocalDate fecha) {
        this.usuario = usuario;
        this.puntoVenta = puntoVenta;
        this.fecha = fecha;
    }

    public Integer getIdAsignacion() { return idAsignacion; }
    public Usuario getUsuario() { return usuario; }
    public PuntoVenta getPuntoVenta() { return puntoVenta; }
    public LocalDate getFecha() { return fecha; }
}
