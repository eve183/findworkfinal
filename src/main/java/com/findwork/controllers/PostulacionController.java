package com.findwork.controllers;

import com.findwork.entities.Postulacion;
import com.findwork.services.PostulacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@CrossOrigin(origins = "*")
public class PostulacionController {

    private final PostulacionService service;

    public PostulacionController(PostulacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Postulacion> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Postulacion obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Postulacion> crear(@RequestBody Postulacion postulacion) {
        return new ResponseEntity<>(service.save(postulacion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Postulacion actualizar(@PathVariable Integer id, @RequestBody Postulacion postulacion) {
        return service.update(id, postulacion);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
