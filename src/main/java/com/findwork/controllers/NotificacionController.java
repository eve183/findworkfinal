package com.findwork.controllers;

import com.findwork.entities.Notificacion;
import com.findwork.services.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notificacion> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Notificacion obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Notificacion> crear(@Valid @RequestBody Notificacion notificacion) {
        return new ResponseEntity<>(service.save(notificacion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Notificacion actualizar(@PathVariable Integer id, @RequestBody Notificacion notificacion) {
        return service.update(id, notificacion);
    }

    @PatchMapping("/{id}/leer")
    public Notificacion marcarLeida(@PathVariable Integer id) {
        return service.marcarLeida(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
