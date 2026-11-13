package com.madremasa.service;

import com.madremasa.model.DetalleReceta;
import com.madremasa.model.Receta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Cálculo del costo de las recetas (RF-14, RN-08)
@Service
public class RecetaService {

    // Suma de la cantidad de cada ingrediente por su costo unitario vigente
    public BigDecimal costoPorTanda(Receta receta) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleReceta detalle : receta.getDetalles()) {
            total = total.add(detalle.getCosto());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // Costo de la tanda dividido entre el rendimiento
    public BigDecimal costoPorUnidad(Receta receta) {
        return costoPorTanda(receta).divide(BigDecimal.valueOf(receta.getRendimiento()), 2, RoundingMode.HALF_UP);
    }

    // Margen estimado en porcentaje respecto al precio de venta (RF-15)
    public BigDecimal margen(Receta receta) {
        BigDecimal precio = receta.getProducto().getPrecioVenta();
        if (precio.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return precio.subtract(costoPorUnidad(receta))
                .multiply(BigDecimal.valueOf(100))
                .divide(precio, 1, RoundingMode.HALF_UP);
    }
}
