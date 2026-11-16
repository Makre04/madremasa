package com.madremasa.service;

import com.madremasa.model.Rol;
import com.madremasa.model.Usuario;
import com.madremasa.repository.RolRepositorio;
import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Registro y mantenimiento de empleados (RF-04, RF-05) e intentos de inicio de sesión (RNF-03)
@Service
public class UsuarioService {

    public static final int MAXIMO_INTENTOS = 5;
    public static final int MINUTOS_BLOQUEO = 15;

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
    }

    // Registra un empleado nuevo. RN-01: el código es único y tiene el formato EMP-000.
    @Transactional
    public Usuario crear(String codigo, String nombre, Integer idRol, String contrasena) {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toUpperCase();
        if (!codigoNormalizado.matches("EMP-\\d{3}")) {
            throw new ReglaNegocioException("El código debe tener el formato EMP-000, por ejemplo EMP-005.");
        }
        if (usuarioRepositorio.existsByCodigoEmpleado(codigoNormalizado)) {
            throw new ReglaNegocioException("El código " + codigoNormalizado + " ya pertenece a otro empleado.");
        }
        validarContrasena(contrasena);

        Usuario usuario = new Usuario();
        usuario.setCodigoEmpleado(codigoNormalizado);
        usuario.setNombreCompleto(validarNombre(nombre));
        usuario.setRol(buscarRol(idRol));
        usuario.setContrasenaHash(codificador.encode(contrasena));
        usuario.setActivo(true);
        usuario.setIntentosFallidos(0);
        return usuarioRepositorio.save(usuario);
    }

    // Edita el nombre y el rol. Si se indica una contraseña nueva, se restablece (RF-05).
    @Transactional
    public Usuario actualizar(Integer idUsuario, String nombre, Integer idRol, String contrasenaNueva) {
        Usuario usuario = buscar(idUsuario);
        usuario.setNombreCompleto(validarNombre(nombre));
        usuario.setRol(buscarRol(idRol));
        if (contrasenaNueva != null && !contrasenaNueva.isBlank()) {
            validarContrasena(contrasenaNueva);
            usuario.setContrasenaHash(codificador.encode(contrasenaNueva));
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }
        return usuario;
    }

    // Activa o desactiva un empleado. El código no se reasigna (RN-01).
    @Transactional
    public Usuario cambiarEstado(Integer idUsuario, Integer idUsuarioActual) {
        Usuario usuario = buscar(idUsuario);
        if (usuario.getIdUsuario().equals(idUsuarioActual)) {
            throw new ReglaNegocioException("No puede desactivar su propio usuario.");
        }
        usuario.setActivo(!usuario.getActivo());
        return usuario;
    }

    // Indica si el usuario está bloqueado temporalmente por intentos fallidos
    public boolean estaBloqueado(Usuario usuario) {
        return usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now());
    }

    // RNF-03: después de cinco intentos fallidos, la cuenta se bloquea durante 15 minutos
    @Transactional
    public void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        if (intentos >= MAXIMO_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
            intentos = 0;
        }
        usuario.setIntentosFallidos(intentos);
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void registrarIngresoCorrecto(Usuario usuario) {
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepositorio.save(usuario);
    }

    public boolean contrasenaCorrecta(Usuario usuario, String contrasena) {
        return codificador.matches(contrasena, usuario.getContrasenaHash());
    }

    private Usuario buscar(Integer idUsuario) {
        return usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("El empleado solicitado no existe."));
    }

    private Rol buscarRol(Integer idRol) {
        if (idRol == null) {
            throw new ReglaNegocioException("Seleccione el rol del empleado.");
        }
        return rolRepositorio.findById(idRol)
                .orElseThrow(() -> new ReglaNegocioException("El rol seleccionado no existe."));
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("Ingrese el nombre completo del empleado.");
        }
        if (nombre.trim().length() > 100) {
            throw new ReglaNegocioException("El nombre no puede superar 100 caracteres.");
        }
        return nombre.trim();
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres.");
        }
    }
}
