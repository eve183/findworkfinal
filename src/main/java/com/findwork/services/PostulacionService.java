package com.findwork.services;

import com.findwork.entities.EstadoOferta;
import com.findwork.entities.EstadoPostulacion;
import com.findwork.entities.EstadoVerificacion;
import com.findwork.entities.OfertaLaboral;
import com.findwork.entities.Postulacion;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.OfertaLaboralRepository;
import com.findwork.repositories.PostulacionRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostulacionService {

    private final PostulacionRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final SeguridadActual seguridadActual;

    public PostulacionService(PostulacionRepository repository,
                              UsuarioRepository usuarioRepository,
                              OfertaLaboralRepository ofertaRepository,
                              SeguridadActual seguridadActual) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.ofertaRepository = ofertaRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<Postulacion> findAll() {
        Usuario actual = seguridadActual.getActual();
        if (seguridadActual.esAdmin(actual)) {
            return repository.findAll();
        }
        if (seguridadActual.esCandidato(actual)) {
            return repository.findByUsuarioIdUsuario(actual.getIdUsuario());
        }
        if (seguridadActual.esEmpleador(actual)) {
            return repository.findByOferta_Empresa_Empleador_IdUsuario(actual.getIdUsuario());
        }
        throw new AccesoDenegadoException("No tiene permisos");
    }

    public Postulacion findById(Integer id) {
        Postulacion postulacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Postulación no encontrada con id: " + id));
        Usuario actual = seguridadActual.getActual();
        boolean propia = postulacion.getUsuario() != null && seguridadActual.esDueno(actual, postulacion.getUsuario().getIdUsuario());
        boolean recibida = postulacion.getOferta() != null && postulacion.getOferta().getEmpresa() != null && seguridadActual.esDueno(actual, postulacion.getOferta().getEmpresa().getEmpleador().getIdUsuario());
        if (!seguridadActual.esAdmin(actual) && !propia && !recibida) {
            throw new AccesoDenegadoException("No tiene permisos para ver esta postulación");
        }
        return postulacion;
    }

    public Postulacion save(Postulacion postulacion) {
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esCandidato(actual)) {
            throw new AccesoDenegadoException("Solo un candidato puede postularse");
        }
        if (postulacion.getOferta() == null || postulacion.getOferta().getIdOferta() == null) {
            throw new AccesoDenegadoException("La oferta es obligatoria");
        }
        OfertaLaboral oferta = ofertaRepository.findById(postulacion.getOferta().getIdOferta())
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada"));
        if (oferta.getEstado() != EstadoOferta.activa || oferta.getEmpresa() == null || oferta.getEmpresa().getEstadoVerificacion() != EstadoVerificacion.verificado) {
            throw new AccesoDenegadoException("Solo se puede postular a ofertas activas de empresas verificadas");
        }
        postulacion.setUsuario(actual);
        postulacion.setOferta(oferta);
        postulacion.setEstado(EstadoPostulacion.pendiente);
        return repository.save(postulacion);
    }

    public Postulacion update(Integer id, Postulacion datos) {
        Postulacion postulacion = findById(id);
        Usuario actual = seguridadActual.getActual();
        boolean empleadorDueno = postulacion.getOferta() != null && postulacion.getOferta().getEmpresa() != null && seguridadActual.esDueno(actual, postulacion.getOferta().getEmpresa().getEmpleador().getIdUsuario());
        if (!seguridadActual.esAdmin(actual) && !empleadorDueno) {
            throw new AccesoDenegadoException("Solo el empleador dueño de la oferta o el administrador puede actualizarla");
        }
        if (datos.getDescripcion() != null) postulacion.setDescripcion(datos.getDescripcion());
        if (datos.getEstado() != null) postulacion.setEstado(datos.getEstado());
        return repository.save(postulacion);
    }

    public void delete(Integer id) {
        Postulacion postulacion = findById(id);
        Usuario actual = seguridadActual.getActual();
        boolean candidatoDueno = postulacion.getUsuario() != null && seguridadActual.esDueno(actual, postulacion.getUsuario().getIdUsuario());
        if (!seguridadActual.esAdmin(actual) && !candidatoDueno) {
            throw new AccesoDenegadoException("Solo el candidato dueño o el administrador puede eliminar");
        }
        repository.deleteById(id);
    }
}
