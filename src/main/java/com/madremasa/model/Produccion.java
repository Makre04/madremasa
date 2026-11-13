package com.madremasa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Elaboración de una receta en el centro de producción
@Entity
@Table(name = "produccion")
public class Produccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produccion")
    private Integer idProduccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @Column(name = "tandas", nullable = false)
    private Integer tandas;

    @Column(name = "unidades_producidas", nullable = false)
    private Integer unidadesProducidas;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @Column(name = "motivo_anulacion", length = 200)
    private String motivoAnulacion;

    public Integer getIdProduccion() { return idProduccion; }
    public Receta getReceta() { return receta; }
    public Integer getTandas() { return tandas; }
    public Integer getUnidadesProducidas() { return unidadesProducidas; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
}
