package com.findwork.services;

import com.findwork.entities.Empresa;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.EmpresaRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final SeguridadActual seguridadActual;

    public EmpresaService(EmpresaRepository repository, UsuarioRepository usuarioRepository,
                          SeguridadActual seguridadActual) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<Empresa> findAll() {
        return repository.findByEstadoVerificacion(com.findwork.entities.EstadoVerificacion.verificado);
    }

    public Empresa findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada con id: " + id));
    }

    public List<Empresa> findByEmpleador(Integer empleadorId) {
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, empleadorId)) {
            throw new AccesoDenegadoException("Solo puede consultar sus propias empresas");
        }
        return repository.findByEmpleadorIdUsuario(empleadorId);
    }

    public Empresa save(Empresa empresa) {
        Usuario empleador = seguridadActual.getActual();
        empresa.setEmpleador(empleador);
        empresa.setEstadoVerificacion(com.findwork.entities.EstadoVerificacion.pendiente);
        return repository.save(empresa);
    }

    public Empresa update(Integer id, Empresa datos) {
        Empresa empresa = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, empresa.getEmpleador().getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede editar sus propias empresas");
        }
        if (datos.getNit() != null) empresa.setNit(datos.getNit());
        if (datos.getRazonSocial() != null) empresa.setRazonSocial(datos.getRazonSocial());
        if (datos.getNombreComercial() != null) empresa.setNombreComercial(datos.getNombreComercial());
        if (datos.getDireccion() != null) empresa.setDireccion(datos.getDireccion());
        if (datos.getTelefono() != null) empresa.setTelefono(datos.getTelefono());
        if (datos.getCorreo() != null) empresa.setCorreo(datos.getCorreo());
        if (datos.getLogoUrl() != null) empresa.setLogoUrl(datos.getLogoUrl());
        if (datos.getSitioWeb() != null) empresa.setSitioWeb(datos.getSitioWeb());
        if (datos.getDescripcion() != null) empresa.setDescripcion(datos.getDescripcion());
        if (datos.getEstadoVerificacion() != null) {
            if (!seguridadActual.esAdmin(actual)) {
                throw new AccesoDenegadoException("Solo el administrador puede cambiar el estado de verificación");
            }
            empresa.setEstadoVerificacion(datos.getEstadoVerificacion());
        }
        return repository.save(empresa);
    }

    public void delete(Integer id) {
        Empresa empresa = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && !seguridadActual.esDueno(actual, empresa.getEmpleador().getIdUsuario())) {
            throw new AccesoDenegadoException("Solo puede eliminar sus propias empresas");
        }
        repository.deleteById(id);
    }
}
