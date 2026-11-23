package com.findwork.controllers;

import com.findwork.entities.Perfil;
import com.findwork.services.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
@CrossOrigin(origins = "*")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public List<Perfil> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Perfil obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Perfil> crear(@Valid @RequestBody Perfil perfil) {
        return new ResponseEntity<>(service.save(perfil), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Perfil actualizar(@PathVariable Integer id, @RequestBody Perfil perfil) {
        return service.update(id, perfil);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
