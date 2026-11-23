package com.findwork.services;

import com.findwork.entities.Categoria;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<Categoria> findAll() {
        return repository.findAll();
    }

    public Categoria findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));
    }

    public Categoria save(Categoria categoria) {
        return repository.save(categoria);
    }

    public Categoria update(Integer id, Categoria datos) {
        Categoria categoria = findById(id);
        categoria.setNombre(datos.getNombre());
        categoria.setDescripcion(datos.getDescripcion());
        return repository.save(categoria);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
