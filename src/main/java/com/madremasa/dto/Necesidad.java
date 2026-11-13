package com.madremasa.dto;

import java.math.BigDecimal;

// Resultado del cálculo de insumos para una producción (RF-17 y RF-19)
public record Necesidad(String insumo, String simbolo, BigDecimal requerido,
                        BigDecimal disponible, BigDecimal faltante) {

    public boolean alcanza() {
        return faltante.signum() == 0;
    }
}
