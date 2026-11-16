package com.madremasa.dto;

import java.math.BigDecimal;

// Producción confirmada de un producto en un período con su costo registrado (RF-25)
public record CostoProduccion(String producto, Long producciones, Long tandas, Long unidades, BigDecimal costo) {
}
