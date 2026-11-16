package com.madremasa.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Cierre de jornada de un punto de venta
@Entity
@Table(name = "cierre_venta")
public class CierreVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Integer idCierre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_punto_venta")
    private PuntoVenta puntoVenta;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "monto_efectivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoEfectivo;

    @Column(name = "monto_datafono", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDatafono;

    // Columna calculada en SQL Server (efectivo + datáfono); solo se lee
    @Column(name = "total", insertable = false, updatable = false, precision = 13, scale = 2)
    private BigDecimal total;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public Integer getIdCierre() { return idCierre; }
    public PuntoVenta getPuntoVenta() { return puntoVenta; }
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getMontoEfectivo() { return montoEfectivo; }
    public BigDecimal getMontoDatafono() { return montoDatafono; }
    public BigDecimal getTotal() { return total; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    // Productos vendidos en este cierre
    @OneToMany(mappedBy = "cierre")
    private List<DetalleCierre> detalles = new ArrayList<>();

    public List<DetalleCierre> getDetalles() { return detalles; }
    public void setPuntoVenta(PuntoVenta puntoVenta) { this.puntoVenta = puntoVenta; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setMontoEfectivo(BigDecimal montoEfectivo) { this.montoEfectivo = montoEfectivo; }
    public void setMontoDatafono(BigDecimal montoDatafono) { this.montoDatafono = montoDatafono; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
