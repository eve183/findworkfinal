package com.findwork.services;

import com.findwork.entities.Notificacion;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.AccesoDenegadoException;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.repositories.NotificacionRepository;
import com.findwork.repositories.UsuarioRepository;
import com.findwork.security.SeguridadActual;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final SeguridadActual seguridadActual;

    public NotificacionService(NotificacionRepository repository, UsuarioRepository usuarioRepository,
                               SeguridadActual seguridadActual) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.seguridadActual = seguridadActual;
    }

    public List<Notificacion> findAll() {
        Usuario actual = seguridadActual.getActual();
        if (seguridadActual.esAdmin(actual)) {
            return repository.findAll();
        }
        return repository.findByUsuarioIdUsuario(actual.getIdUsuario());
    }

    public Notificacion findById(Integer id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada con id: " + id));
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (notificacion.getUsuario() == null || !seguridadActual.esDueno(actual, notificacion.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede ver sus propias notificaciones");
        }
        return notificacion;
    }

    public Notificacion save(Notificacion notificacion) {
        if (notificacion.getUsuario() != null && notificacion.getUsuario().getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(notificacion.getUsuario().getIdUsuario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            notificacion.setUsuario(usuario);
        }
        if (notificacion.getLeida() == null) {
            notificacion.setLeida(false);
        }
        return repository.save(notificacion);
    }

    public Notificacion update(Integer id, Notificacion datos) {
        Notificacion notificacion = findById(id);
        Notificacion actualNotificacion = notificacion;
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (actualNotificacion.getUsuario() == null || !seguridadActual.esDueno(actual, actualNotificacion.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede actualizar sus propias notificaciones");
        }
        if (datos.getTipo() != null) notificacion.setTipo(datos.getTipo());
        if (datos.getTitulo() != null) notificacion.setTitulo(datos.getTitulo());
        if (datos.getMensaje() != null) notificacion.setMensaje(datos.getMensaje());
        if (datos.getLeida() != null) notificacion.setLeida(datos.getLeida());
        return repository.save(notificacion);
    }

    public Notificacion marcarLeida(Integer id) {
        Notificacion notificacion = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (notificacion.getUsuario() == null || !seguridadActual.esDueno(actual, notificacion.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede marcar sus propias notificaciones");
        }
        notificacion.setLeida(true);
        return repository.save(notificacion);
    }

    public void delete(Integer id) {
        Notificacion notificacion = findById(id);
        Usuario actual = seguridadActual.getActual();
        if (!seguridadActual.esAdmin(actual) && (notificacion.getUsuario() == null || !seguridadActual.esDueno(actual, notificacion.getUsuario().getIdUsuario()))) {
            throw new AccesoDenegadoException("Solo puede eliminar sus propias notificaciones");
        }
        repository.deleteById(id);
    }
}
