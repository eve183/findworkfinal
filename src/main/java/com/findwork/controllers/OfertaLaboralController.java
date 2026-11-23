package com.findwork.controllers;

import com.findwork.entities.OfertaLaboral;
import com.findwork.services.OfertaLaboralService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
@CrossOrigin(origins = "*")
public class OfertaLaboralController {

    private final OfertaLaboralService service;

    public OfertaLaboralController(OfertaLaboralService service) {
        this.service = service;
    }

    @GetMapping
    public List<OfertaLaboral> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public OfertaLaboral obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<OfertaLaboral> crear(@Valid @RequestBody OfertaLaboral oferta) {
        return new ResponseEntity<>(service.save(oferta), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public OfertaLaboral actualizar(@PathVariable Integer id, @RequestBody OfertaLaboral oferta) {
        return service.update(id, oferta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
