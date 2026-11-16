package com.madremasa.service;

import com.madremasa.model.Asignacion;
import com.madremasa.model.PuntoVenta;
import com.madremasa.model.Usuario;
import com.madremasa.repository.AsignacionRepositorio;
import com.madremasa.repository.PuntoVentaRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import com.madremasa.sesion.ControlAcceso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

// Puntos de venta y asignación de empleados por fecha (RF-06, RF-07)
@Service
public class PuntoVentaService {

    private final PuntoVentaRepositorio puntoRepositorio;
    private final AsignacionRepositorio asignacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public PuntoVentaService(PuntoVentaRepositorio puntoRepositorio, AsignacionRepositorio asignacionRepositorio,
                             UsuarioRepositorio usuarioRepositorio) {
        this.puntoRepositorio = puntoRepositorio;
        this.asignacionRepositorio = asignacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // Crea (idPuntoVenta nulo) o actualiza un punto de venta con sus días de operación
    @Transactional
    public PuntoVenta guardar(Integer idPuntoVenta, String nombre, String tipo, String ubicacion,
                              List<Integer> dias, boolean activo) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Ingrese el nombre del punto de venta.");
        }
        if (!"CENTRO".equals(tipo) && !"FERIA".equals(tipo)) {
            throw new ReglaNegocioException("Seleccione si es el centro de producción o una feria.");
        }
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new ReglaNegocioException("Ingrese la ubicación del punto de venta.");
        }
        if (dias == null || dias.isEmpty()) {
            throw new ReglaNegocioException("Seleccione al menos un día de operación.");
        }
        boolean repetido = idPuntoVenta == null
                ? puntoRepositorio.existsByNombreIgnoreCase(nombre.trim())
                : puntoRepositorio.existsByNombreIgnoreCaseAndIdPuntoVentaNot(nombre.trim(), idPuntoVenta);
        if (repetido) {
            throw new ReglaNegocioException("Ya existe un punto de venta llamado \"" + nombre.trim() + "\".");
        }

        PuntoVenta punto = idPuntoVenta == null ? new PuntoVenta() : buscar(idPuntoVenta);
        punto.setNombre(nombre.trim());
        punto.setTipo(tipo);
        punto.setUbicacion(ubicacion.trim());
        punto.setActivo(activo);
        punto.getDias().clear();
        punto.getDias().addAll(new TreeSet<>(dias));
        return puntoRepositorio.save(punto);
    }

    // Asigna un empleado de punto de venta a una feria para una fecha (un lugar por día)
    @Transactional
    public Asignacion asignar(Integer idUsuario, Integer idPuntoVenta, LocalDate fecha) {
        if (idUsuario == null || idPuntoVenta == null || fecha == null) {
            throw new ReglaNegocioException("Seleccione el empleado, el punto de venta y la fecha.");
        }
        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("El empleado seleccionado no existe."));
        if (!ControlAcceso.PUNTO_VENTA.equals(usuario.getRol().getNombre()) || !usuario.getActivo()) {
            throw new ReglaNegocioException("Solo se pueden asignar empleados activos con rol de punto de venta.");
        }
        PuntoVenta punto = buscar(idPuntoVenta);
        if (!punto.getActivo()) {
            throw new ReglaNegocioException("El punto de venta seleccionado está inactivo.");
        }
        if (asignacionRepositorio.existsByUsuarioIdUsuarioAndFecha(idUsuario, fecha)) {
            throw new ReglaNegocioException(usuario.getCodigoEmpleado() + " ya tiene un punto de venta asignado ese día.");
        }
        return asignacionRepositorio.save(new Asignacion(usuario, punto, fecha));
    }

    public PuntoVenta buscar(Integer idPuntoVenta) {
        return puntoRepositorio.findById(idPuntoVenta)
                .orElseThrow(() -> new ReglaNegocioException("El punto de venta solicitado no existe."));
    }
}
