package com.madremasa.model;

import jakarta.persistence.*;
import java.util.TreeSet;
import java.util.Set;

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

    // Días de la semana en que opera (1 = lunes ... 7 = domingo), guardados en la tabla dia_operacion
    @ElementCollection
    @CollectionTable(name = "dia_operacion", joinColumns = @JoinColumn(name = "id_punto_venta"))
    @Column(name = "dia_semana")
    private Set<Integer> dias = new TreeSet<>();

    public Set<Integer> getDias() { return dias; }
    public void setDias(Set<Integer> dias) { this.dias = dias; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    // Días de operación en texto, por ejemplo "Vie, Sáb"
    public String getDiasTexto() {
        String[] nombres = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        StringBuilder texto = new StringBuilder();
        for (Integer dia : dias) {
            if (texto.length() > 0) texto.append(", ");
            texto.append(nombres[dia - 1]);
        }
        return texto.toString();
    }
}
