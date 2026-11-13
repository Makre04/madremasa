package com.madremasa.dto;

import java.math.BigDecimal;

// Fila del reporte de ventas por punto de venta
public record ResumenPuntoVenta(String puntoVenta, Long cierres, BigDecimal efectivo,
                                BigDecimal datafono, BigDecimal total) {
}
