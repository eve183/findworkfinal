package com.findwork.services;

import com.findwork.entities.Empresa;
import com.findwork.entities.Resena;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.EmpresaRepository;
import com.findwork.repositories.ResenaRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResenaService {

    private final ResenaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SeguridadActual seguridadActual;

    public ResenaService(ResenaRepository repository,
                         UsuarioRepository usuarioRepository,
                         EmpresaRepository empresaRepository,
                         SeguridadActual seguridadActual) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<Resena> findAll() {
        return repository.findAll();
    }

    public Resena findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada con id: " + id));
    }

    public Resena save(Resena resena) {
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esCandidato(actual)) {
            throw new AccesoDenegadoException("Solo un candidato puede escribir una reseña");
        }
        if (resena.getEmpresa() == null || resena.getEmpresa().getIdEmpresa() == null) {
            throw new AccesoDenegadoException("La empresa es obligatoria");
        }
        Empresa empresa = empresaRepository.findById(resena.getEmpresa().getIdEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        resena.setUsuario(actual);
        resena.setEmpresa(empresa);
        return repository.save(resena);
    }

    public Resena update(Integer id, Resena datos) {
        Resena resena = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (resena.getUsuario() == null || !seguridadActual.esDueno(actual, resena.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede editar sus propias reseñas");
        }
        if (datos.getComentario() != null) resena.setComentario(datos.getComentario());
        return repository.save(resena);
    }

    public void delete(Integer id) {
        Resena resena = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (resena.getUsuario() == null || !seguridadActual.esDueno(actual, resena.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede eliminar sus propias reseñas");
        }
        repository.deleteById(id);
    }
}
