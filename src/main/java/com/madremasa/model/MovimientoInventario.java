package com.madremasa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Movimiento que cambia la existencia de un insumo: entrada, producción o ajuste (RF-09, RF-10, RF-11)
@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    // ENTRADA, PRODUCCION, AJUSTE o ANULACION
    @Column(name = "tipo", nullable = false, length = 12)
    private String tipo;

    // Positiva suma a la existencia; negativa resta
    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    // Solo en las entradas por compra
    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_produccion")
    private Produccion produccion;

    // RNF-06: cada movimiento guarda quién lo hizo y cuándo
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public MovimientoInventario() {
    }

    public MovimientoInventario(Insumo insumo, String tipo, BigDecimal cantidad, Usuario usuario) {
        this.insumo = insumo;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public Integer getIdMovimiento() { return idMovimiento; }
    public Insumo getInsumo() { return insumo; }
    public String getTipo() { return tipo; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public String getMotivo() { return motivo; }
    public Produccion getProduccion() { return produccion; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFecha() { return fecha; }

    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setProduccion(Produccion produccion) { this.produccion = produccion; }

    // Nombre legible del tipo para las vistas
    public String getTipoVisible() {
        return switch (tipo) {
            case "ENTRADA" -> "Entrada";
            case "PRODUCCION" -> "Producción";
            case "AJUSTE" -> "Ajuste";
            case "ANULACION" -> "Anulación";
            default -> tipo;
        };
    }
}
