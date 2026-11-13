package com.madremasa.service;

import com.madremasa.model.Usuario;
import com.madremasa.repository.UsuarioRepositorio;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Busca al empleado por su código en la base de datos para iniciar sesión
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioDetallesService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String codigo) throws UsernameNotFoundException {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toUpperCase();
        Usuario usuario = usuarioRepositorio.findByCodigoEmpleado(codigoNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Código no registrado"));

        // RN-02: solo los usuarios activos pueden ingresar
        return User.withUsername(usuario.getCodigoEmpleado())
                .password(usuario.getContrasenaHash())
                .roles(usuario.getRol().getNombre())
                .disabled(!usuario.getActivo())
                .build();
    }
}
