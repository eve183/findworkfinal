package com.findwork.services;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.CorreoYaRegistradoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final RolUsuarioRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeguridadActual seguridadActual;

    public UsuarioService(UsuarioRepository repository, RolUsuarioRepository rolRepository,
                          PasswordEncoder passwordEncoder, SeguridadActual seguridadActual) {
        this.repository = repository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.seguridadActual = seguridadActual;
    }

    public List<Usuario> findAll() {
        if (!seguridadActual.esAdmin(seguridadActual.getActual())) {
            throw new AccesoDenegadoException("Solo el administrador puede listar usuarios");
        }
        return repository.findAll();
    }

    public Usuario findById(Integer id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, usuario.getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede consultar su propia cuenta");
        }
        return usuario;
    }

    public Usuario save(Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
            RolUsuario rol = rolRepository.findById(usuario.getRol().getIdRol())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
            if ("ADMINISTRADOR".equalsIgnoreCase(rol.getNombre())) {
                throw new AccesoDenegadoException("No se puede registrar un administrador por API pública");
            }
            usuario.setRol(rol);
        }
        if (repository.existsByCorreo(usuario.getCorreo())) {
            throw new CorreoYaRegistradoException("El correo ya está registrado: " + usuario.getCorreo());
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return repository.save(usuario);
    }

    public Usuario update(Integer id, Usuario datos) {
        Usuario actual = seguridadActual.getActual();
        Usuario usuario = findById(id);
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, usuario.getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede editar su propia cuenta");
        }
        if (datos.getCorreo() != null) usuario.setCorreo(datos.getCorreo());
        if (datos.getPassword() != null) usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        if (datos.getRol() != null && datos.getRol().getIdRol() != null) {
            if (!seguridadActual.esAdmin(actual)) {
                throw new AccesoDenegadoException("Solo el administrador puede cambiar el rol");
            }
            RolUsuario rol = rolRepository.findById(datos.getRol().getIdRol())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
            usuario.setRol(rol);
        }
        return repository.save(usuario);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
