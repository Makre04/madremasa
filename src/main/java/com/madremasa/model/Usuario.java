package com.madremasa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Empleado o administradora que utiliza el sistema
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "codigo_empleado", nullable = false, length = 10)
    private String codigoEmpleado;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    // Solo se guarda el hash BCrypt de la contraseña
    @Column(name = "contrasena_hash", nullable = false, length = 60)
    private String contrasenaHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Integer getIdUsuario() { return idUsuario; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getContrasenaHash() { return contrasenaHash; }
    public Rol getRol() { return rol; }
    public Boolean getActivo() { return activo; }
    public Integer getIntentosFallidos() { return intentosFallidos; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public void setRol(Rol rol) { this.rol = rol; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}
