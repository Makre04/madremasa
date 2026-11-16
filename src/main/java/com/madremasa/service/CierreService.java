package com.madremasa.service;

import com.madremasa.model.Asignacion;
import com.madremasa.model.CierreVenta;
import com.madremasa.model.DetalleCierre;
import com.madremasa.model.Producto;
import com.madremasa.repository.AsignacionRepositorio;
import com.madremasa.repository.CierreVentaRepositorio;
import com.madremasa.repository.DetalleCierreRepositorio;
import com.madremasa.repository.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Cierre de jornada de un punto de venta (RF-21, RF-22, RN-14, RN-15)
@Service
public class CierreService {

    private final AsignacionRepositorio asignacionRepositorio;
    private final CierreVentaRepositorio cierreRepositorio;
    private final DetalleCierreRepositorio detalleRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public CierreService(AsignacionRepositorio asignacionRepositorio, CierreVentaRepositorio cierreRepositorio,
                         DetalleCierreRepositorio detalleRepositorio, ProductoRepositorio productoRepositorio) {
        this.asignacionRepositorio = asignacionRepositorio;
        this.cierreRepositorio = cierreRepositorio;
        this.detalleRepositorio = detalleRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    // Asignación del empleado para hoy, si existe
    public Optional<Asignacion> asignacionDeHoy(Integer idUsuario) {
        return asignacionRepositorio.findByUsuarioIdUsuarioAndFecha(idUsuario, LocalDate.now());
    }

    // Indica si el punto de venta ya tiene cierre en la fecha
    public boolean yaCerrado(Asignacion asignacion) {
        return cierreRepositorio.existsByPuntoVentaIdPuntoVentaAndFecha(
                asignacion.getPuntoVenta().getIdPuntoVenta(), asignacion.getFecha());
    }

    // Registra el cierre del punto asignado hoy con los montos y las unidades vendidas por producto.
    // Las listas de productos y unidades vienen en el mismo orden desde el formulario.
    @Transactional
    public CierreVenta registrar(Integer idUsuario, BigDecimal efectivo, BigDecimal datafono,
                                 List<Integer> idsProducto, List<Integer> unidades) {
        // RN-14: solo puede cerrar el punto y la fecha que tiene asignados
        Asignacion asignacion = asignacionDeHoy(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("No tiene un punto de venta asignado para hoy."));
        // RN-15: un solo cierre por punto de venta y fecha
        if (yaCerrado(asignacion)) {
            throw new ReglaNegocioException("El cierre de " + asignacion.getPuntoVenta().getNombre()
                    + " para hoy ya fue registrado.");
        }
        validarMontosYUnidades(efectivo, datafono, idsProducto, unidades);

        CierreVenta cierre = new CierreVenta();
        cierre.setPuntoVenta(asignacion.getPuntoVenta());
        cierre.setFecha(asignacion.getFecha());
        cierre.setMontoEfectivo(efectivo);
        cierre.setMontoDatafono(datafono);
        cierre.setUsuario(asignacion.getUsuario());
        cierreRepositorio.save(cierre);

        for (int i = 0; i < idsProducto.size(); i++) {
            Producto producto = productoRepositorio.findById(idsProducto.get(i))
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos no existe."));
            DetalleCierre detalle = new DetalleCierre(cierre, producto, unidades.get(i));
            cierre.getDetalles().add(detalle);
            detalleRepositorio.save(detalle);
        }
        return cierre;
    }

    // RF-22: el administrador corrige los montos y las unidades de un cierre ya registrado.
    // El punto de venta y la fecha no cambian, así se mantiene un solo cierre por punto y fecha (RN-15).
    @Transactional
    public CierreVenta corregir(Integer idCierre, BigDecimal efectivo, BigDecimal datafono,
                                List<Integer> idsProducto, List<Integer> unidades) {
        CierreVenta cierre = cierreRepositorio.findById(idCierre)
                .orElseThrow(() -> new ReglaNegocioException("El cierre no existe."));
        validarMontosYUnidades(efectivo, datafono, idsProducto, unidades);

        cierre.setMontoEfectivo(efectivo);
        cierre.setMontoDatafono(datafono);
        cierreRepositorio.save(cierre);

        for (int i = 0; i < idsProducto.size(); i++) {
            Integer idProducto = idsProducto.get(i);
            Integer cantidad = unidades.get(i);
            DetalleCierre existente = cierre.getDetalles().stream()
                    .filter(d -> d.getProducto().getIdProducto().equals(idProducto))
                    .findFirst().orElse(null);
            if (existente != null) {
                existente.setUnidadesVendidas(cantidad);
                detalleRepositorio.save(existente);
            } else if (cantidad > 0) {
                // Producto que no se había anotado en el cierre original
                Producto producto = productoRepositorio.findById(idProducto)
                        .orElseThrow(() -> new ReglaNegocioException("Uno de los productos no existe."));
                DetalleCierre detalle = new DetalleCierre(cierre, producto, cantidad);
                cierre.getDetalles().add(detalle);
                detalleRepositorio.save(detalle);
            }
        }
        return cierre;
    }

    // RN-15: montos obligatorios y no negativos; unidades no negativas
    private void validarMontosYUnidades(BigDecimal efectivo, BigDecimal datafono,
                                        List<Integer> idsProducto, List<Integer> unidades) {
        if (efectivo == null || datafono == null || efectivo.signum() < 0 || datafono.signum() < 0) {
            throw new ReglaNegocioException("Los montos en efectivo y datáfono son obligatorios y no pueden ser negativos.");
        }
        if (idsProducto == null || unidades == null || idsProducto.size() != unidades.size()) {
            throw new ReglaNegocioException("Indique las unidades vendidas de cada producto.");
        }
        for (Integer cantidad : unidades) {
            if (cantidad == null || cantidad < 0) {
                throw new ReglaNegocioException("Las unidades vendidas no pueden estar vacías ni ser negativas.");
            }
        }
    }
}
