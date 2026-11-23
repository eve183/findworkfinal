package com.findwork.controllers;

import com.findwork.entities.Resena;
import com.findwork.services.ResenaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resenas")
@CrossOrigin(origins = "*")
public class ResenaController {

    private final ResenaService service;

    public ResenaController(ResenaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resena> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Resena obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Resena> crear(@RequestBody Resena resena) {
        return new ResponseEntity<>(service.save(resena), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Resena actualizar(@PathVariable Integer id, @RequestBody Resena resena) {
        return service.update(id, resena);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
