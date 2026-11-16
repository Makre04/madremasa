package com.madremasa.service;

import com.madremasa.model.DetalleReceta;
import com.madremasa.model.DetalleRecetaId;
import com.madremasa.model.Insumo;
import com.madremasa.model.Producto;
import com.madremasa.model.Receta;
import com.madremasa.repository.DetalleRecetaRepositorio;
import com.madremasa.repository.InsumoRepositorio;
import com.madremasa.repository.ProductoRepositorio;
import com.madremasa.repository.RecetaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Registro de recetas y cálculo de su costo (RF-13, RF-14, RF-15, RN-08, RN-09)
@Service
public class RecetaService {

    private final RecetaRepositorio recetaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final InsumoRepositorio insumoRepositorio;
    private final DetalleRecetaRepositorio detalleRepositorio;

    public RecetaService(RecetaRepositorio recetaRepositorio, ProductoRepositorio productoRepositorio,
                         InsumoRepositorio insumoRepositorio, DetalleRecetaRepositorio detalleRepositorio) {
        this.recetaRepositorio = recetaRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.insumoRepositorio = insumoRepositorio;
        this.detalleRepositorio = detalleRepositorio;
    }

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

    // Crea el producto y su receta con el primer ingrediente (RN-09: al menos un ingrediente)
    @Transactional
    public Receta crear(String nombreProducto, BigDecimal precio, Integer rendimiento, String instrucciones,
                        Integer idInsumo, BigDecimal cantidad) {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new ReglaNegocioException("Ingrese el nombre del producto.");
        }
        if (productoRepositorio.existsByNombreIgnoreCase(nombreProducto.trim())) {
            throw new ReglaNegocioException("Ya existe un producto llamado \"" + nombreProducto.trim() + "\".");
        }
        validarDatos(precio, rendimiento);

        Producto producto = new Producto();
        producto.setNombre(nombreProducto.trim());
        producto.setPrecioVenta(precio);
        producto.setActivo(true);
        productoRepositorio.save(producto);

        Receta receta = new Receta();
        receta.setProducto(producto);
        receta.setRendimiento(rendimiento);
        receta.setInstrucciones(limpiar(instrucciones));
        receta.setActivo(true);
        recetaRepositorio.save(receta);

        guardarIngrediente(receta, idInsumo, cantidad);
        return receta;
    }

    // Actualiza el precio de venta, el rendimiento y las instrucciones
    @Transactional
    public Receta actualizar(Integer idReceta, BigDecimal precio, Integer rendimiento, String instrucciones) {
        validarDatos(precio, rendimiento);
        Receta receta = buscar(idReceta);
        receta.getProducto().setPrecioVenta(precio);
        receta.setRendimiento(rendimiento);
        receta.setInstrucciones(limpiar(instrucciones));
        return receta;
    }

    // Agrega un ingrediente o cambia su cantidad si ya estaba en la receta
    @Transactional
    public void guardarIngrediente(Integer idReceta, Integer idInsumo, BigDecimal cantidad) {
        guardarIngrediente(buscar(idReceta), idInsumo, cantidad);
    }

    // Quita un ingrediente, siempre que la receta conserve al menos uno (RN-09)
    @Transactional
    public void quitarIngrediente(Integer idReceta, Integer idInsumo) {
        Receta receta = buscar(idReceta);
        if (receta.getDetalles().size() <= 1) {
            throw new ReglaNegocioException("La receta debe conservar al menos un ingrediente.");
        }
        DetalleReceta detalle = detalleRepositorio.findById(new DetalleRecetaId(idReceta, idInsumo))
                .orElseThrow(() -> new ReglaNegocioException("El ingrediente no forma parte de la receta."));
        receta.getDetalles().remove(detalle);
        detalleRepositorio.delete(detalle);
    }

    private void guardarIngrediente(Receta receta, Integer idInsumo, BigDecimal cantidad) {
        if (idInsumo == null) {
            throw new ReglaNegocioException("Seleccione el insumo del ingrediente.");
        }
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ReglaNegocioException("La cantidad del ingrediente debe ser mayor que cero.");
        }
        Insumo insumo = insumoRepositorio.findById(idInsumo)
                .orElseThrow(() -> new ReglaNegocioException("El insumo seleccionado no existe."));
        DetalleReceta detalle = detalleRepositorio.findById(new DetalleRecetaId(receta.getIdReceta(), idInsumo))
                .orElse(null);
        if (detalle == null) {
            detalle = new DetalleReceta(receta, insumo, cantidad);
            receta.getDetalles().add(detalle);
        } else {
            detalle.setCantidad(cantidad);
        }
        detalleRepositorio.save(detalle);
    }

    private Receta buscar(Integer idReceta) {
        return recetaRepositorio.findById(idReceta)
                .orElseThrow(() -> new ReglaNegocioException("La receta solicitada no existe."));
    }

    private void validarDatos(BigDecimal precio, Integer rendimiento) {
        if (precio == null || precio.signum() < 0) {
            throw new ReglaNegocioException("El precio de venta no puede estar vacío ni ser negativo.");
        }
        if (rendimiento == null || rendimiento < 1) {
            throw new ReglaNegocioException("El rendimiento debe ser de al menos una unidad por tanda.");
        }
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
