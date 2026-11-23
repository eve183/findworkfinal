package com.findwork.services;

import com.findwork.entities.Perfil;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.PerfilRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PerfilService {

    private final PerfilRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final SeguridadActual seguridadActual;

    public PerfilService(PerfilRepository repository, UsuarioRepository usuarioRepository,
                         SeguridadActual seguridadActual) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<Perfil> findAll() {
        Usuario actual = seguridadActual.getActual();
        if (seguridadActual.esAdmin(actual)) {
            return repository.findAll();
        }
        return repository.findByUsuarioIdUsuario(actual.getIdUsuario())
                .map(List::of)
                .orElse(List.of());
    }

    public Perfil findById(Integer id) {
        Perfil perfil = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado con id: " + id));
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (perfil.getUsuario() == null || !seguridadActual.esDueno(actual, perfil.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede ver su propio perfil");
        }
        return perfil;
    }

    public Perfil save(Perfil perfil) {
        Usuario actual = seguridadActual.getActual();
        Usuario propietario = seguridadActual.esAdmin(actual) && perfil.getUsuario() != null && perfil.getUsuario().getIdUsuario() != null
                ? usuarioRepository.findById(perfil.getUsuario().getIdUsuario())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"))
                : actual;
        perfil.setUsuario(propietario);
        if (perfil.getEstadoCuenta() == null) {
            perfil.setEstadoCuenta(com.findwork.entities.EstadoCuenta.activo);
        }
        return repository.save(perfil);
    }

    public Perfil update(Integer id, Perfil datos) {
        Perfil perfil = findById(id);
        if (datos.getNombre() != null) perfil.setNombre(datos.getNombre());
        if (datos.getApellido() != null) perfil.setApellido(datos.getApellido());
        if (datos.getFechaNacimiento() != null) perfil.setFechaNacimiento(datos.getFechaNacimiento());
        if (datos.getTelefono() != null) perfil.setTelefono(datos.getTelefono());
        if (datos.getUbicacionActual() != null) perfil.setUbicacionActual(datos.getUbicacionActual());
        if (datos.getEstadoCuenta() != null) perfil.setEstadoCuenta(datos.getEstadoCuenta());
        if (datos.getHojaDeVidaNombre() != null) perfil.setHojaDeVidaNombre(datos.getHojaDeVidaNombre());
        if (datos.getHojaDeVidaUrl() != null) perfil.setHojaDeVidaUrl(datos.getHojaDeVidaUrl());
        return repository.save(perfil);
    }

    public void delete(Integer id) {
        Perfil perfil = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (perfil.getUsuario() == null || !seguridadActual.esDueno(actual, perfil.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede eliminar su propio perfil");
        }
        repository.deleteById(id);
    }
}
