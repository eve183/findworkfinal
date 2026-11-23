package com.findwork.services;

import com.findwork.entities.Categoria;
import com.findwork.entities.Empresa;
import com.findwork.entities.EstadoOferta;
import com.findwork.entities.EstadoVerificacion;
import com.findwork.entities.OfertaLaboral;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.CategoriaRepository;
import com.findwork.repositories.EmpresaRepository;
import com.findwork.repositories.OfertaLaboralRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OfertaLaboralService {

    private final OfertaLaboralRepository repository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SeguridadActual seguridadActual;

    public OfertaLaboralService(OfertaLaboralRepository repository,
                                EmpresaRepository empresaRepository,
                                CategoriaRepository categoriaRepository,
                                SeguridadActual seguridadActual) {
        this.repository = repository;
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<OfertaLaboral> findAll() {
        return repository.findByEstadoAndEmpresa_EstadoVerificacion(EstadoOferta.activa, EstadoVerificacion.verificado);
    }

    public OfertaLaboral findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada con id: " + id));
    }

    public OfertaLaboral save(OfertaLaboral oferta) {
        Usuario actual = seguridadActual.getActual();
        if (oferta.getEmpresa() != null && oferta.getEmpresa().getIdEmpresa() != null) {
            Empresa empresa = empresaRepository.findById(oferta.getEmpresa().getIdEmpresa())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
            if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, empresa.getEmpleador().getIdUsuario())) {
                throw new AccesoDenegadoException("No puede publicar ofertas en una empresa ajena");
            }
            if (empresa.getEstadoVerificacion() != EstadoVerificacion.verificado) {
                throw new AccesoDenegadoException("Solo empresas verificadas pueden publicar ofertas");
            }
            oferta.setEmpresa(empresa);
        } else {
            throw new AccesoDenegadoException("La empresa es obligatoria");
        }
        if (oferta.getCategoria() != null && oferta.getCategoria().getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(oferta.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
            oferta.setCategoria(categoria);
        }
        if (oferta.getEstado() == null) {
            oferta.setEstado(EstadoOferta.activa);
        }
        return repository.save(oferta);
    }

    public OfertaLaboral update(Integer id, OfertaLaboral datos) {
        OfertaLaboral oferta = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, oferta.getEmpresa().getEmpleador().getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede editar ofertas de sus empresas");
        }
        if (datos.getTitulo() != null) oferta.setTitulo(datos.getTitulo());
        if (datos.getDescripcion() != null) oferta.setDescripcion(datos.getDescripcion());
        if (datos.getRequisitos() != null) oferta.setRequisitos(datos.getRequisitos());
        if (datos.getSalario() != null) oferta.setSalario(datos.getSalario());
        if (datos.getTipoVinculacion() != null) oferta.setTipoVinculacion(datos.getTipoVinculacion());
        if (datos.getModalidadTrabajo() != null) oferta.setModalidadTrabajo(datos.getModalidadTrabajo());
        if (datos.getNivelExperiencia() != null) oferta.setNivelExperiencia(datos.getNivelExperiencia());
        if (datos.getJornadaLaboral() != null) oferta.setJornadaLaboral(datos.getJornadaLaboral());
        if (datos.getUbicacion() != null) oferta.setUbicacion(datos.getUbicacion());
        if (datos.getFechaCierre() != null) oferta.setFechaCierre(datos.getFechaCierre());
        if (datos.getEstado() != null) oferta.setEstado(datos.getEstado());
        if (datos.getCategoria() != null && datos.getCategoria().getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(datos.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
            oferta.setCategoria(categoria);
        }
        return repository.save(oferta);
    }

    public void delete(Integer id) {
        OfertaLaboral oferta = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, oferta.getEmpresa().getEmpleador().getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede eliminar ofertas de sus empresas");
        }
        repository.deleteById(id);
    }
}
