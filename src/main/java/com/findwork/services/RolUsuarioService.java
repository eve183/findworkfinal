package com.findwork.services;

import com.findwork.entities.RolUsuario;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.RolUsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolUsuarioService {

    private final RolUsuarioRepository repository;

    public RolUsuarioService(RolUsuarioRepository repository) {
        this.repository = repository;
    }

    public List<RolUsuario> findAll() {
        return repository.findAll();
    }

    public RolUsuario findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con id: " + id));
    }

    public RolUsuario save(RolUsuario rol) {
        return repository.save(rol);
    }

    public RolUsuario update(Integer id, RolUsuario datos) {
        RolUsuario rol = findById(id);
        rol.setNombre(datos.getNombre());
        return repository.save(rol);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
