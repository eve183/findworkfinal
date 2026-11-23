package com.findwork.security;

import com.findwork.entities.Usuario;
import com.findwork.repositories.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindworkUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public FindworkUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String nombreRol = (usuario.getRol() != null && usuario.getRol().getNombre() != null)
                ? usuario.getRol().getNombre().trim().toUpperCase()
                : "CANDIDATO";

        String authority = nombreRol.startsWith("ROLE_") ? nombreRol : "ROLE_" + nombreRol;

        return new User(
                usuario.getCorreo(),       // username = correo
                usuario.getPassword(),     // hash BCrypt
                List.of(new SimpleGrantedAuthority(authority)));
    }
}
