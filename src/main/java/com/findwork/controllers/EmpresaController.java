package com.findwork.controllers;

import com.findwork.entities.Empresa;
import com.findwork.services.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Empresa> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Empresa obtener(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/empleador/{empleadorId}")
    public List<Empresa> porEmpleador(@PathVariable Integer empleadorId) {
        return service.findByEmpleador(empleadorId);
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody Empresa empresa) {
        return new ResponseEntity<>(service.save(empresa), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Empresa actualizar(@PathVariable Integer id, @RequestBody Empresa empresa) {
        return service.update(id, empresa);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.delete(id);
    }
}
