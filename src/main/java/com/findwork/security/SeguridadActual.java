package com.findwork.security;

import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeguridadActual {

    private final UsuarioRepository usuarioRepository;

    public SeguridadActual(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario getActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccesoDenegadoException("Debe autenticarse");
        }
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new AccesoDenegadoException("Usuario autenticado no existe"));
    }

    public boolean esAdmin(Usuario usuario) {
        return usuario != null && usuario.getRol() != null && "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    public boolean esEmpleador(Usuario usuario) {
        return usuario != null && usuario.getRol() != null && "EMPLEADOR".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    public boolean esCandidato(Usuario usuario) {
        return usuario != null && usuario.getRol() != null && "CANDIDATO".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    public boolean esDueno(Usuario actual, Integer idUsuario) {
        return actual != null && actual.getIdUsuario() != null && actual.getIdUsuario().equals(idUsuario);
    }
}
