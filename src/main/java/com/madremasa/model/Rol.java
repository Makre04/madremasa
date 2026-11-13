package com.madremasa.model;

import jakarta.persistence.*;

// Rol que determina las funciones disponibles para cada usuario
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    public Integer getIdRol() { return idRol; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    // Nombre legible del rol para mostrarlo en las vistas
    public String getNombreVisible() {
        return switch (nombre) {
            case "ADMINISTRADOR" -> "Administrador";
            case "PRODUCCION" -> "Producción";
            case "PUNTO_VENTA" -> "Punto de venta";
            default -> nombre;
        };
    }
}
