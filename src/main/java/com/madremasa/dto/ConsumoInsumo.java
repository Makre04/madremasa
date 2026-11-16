package com.madremasa.dto;

import java.math.BigDecimal;

// Cantidad de un insumo consumida por producción en un período y su costo al costo unitario actual (RF-25)
public record ConsumoInsumo(String insumo, String unidad, BigDecimal cantidad, BigDecimal costo) {
}
