package com.madremasa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Datos del formulario de insumos, validados en el servidor (RNF-05)
public class InsumoFormulario {

    private Integer idInsumo;

    @NotBlank(message = "Ingrese el nombre del insumo")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;

    @NotNull(message = "Seleccione la unidad de medida")
    private Integer idUnidad;

    @NotNull(message = "Ingrese la cantidad mínima")
    @DecimalMin(value = "0", message = "La cantidad mínima no puede ser negativa")
    private BigDecimal cantidadMinima;

    @NotNull(message = "Ingrese el costo unitario")
    @DecimalMin(value = "0", message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;

    private boolean activo = true;

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getIdUnidad() { return idUnidad; }
    public void setIdUnidad(Integer idUnidad) { this.idUnidad = idUnidad; }
    public BigDecimal getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(BigDecimal cantidadMinima) { this.cantidadMinima = cantidadMinima; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
