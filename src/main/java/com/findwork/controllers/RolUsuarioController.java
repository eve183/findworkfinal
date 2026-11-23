package com.findwork.controllers;

import com.findwork.entities.RolUsuario;
import com.findwork.services.RolUsuarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolUsuarioController {

    private final RolUsuarioService service;

    public RolUsuarioController(RolUsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<RolUsuario> listar() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RolUsuario obtener(@PathVariable Integer id) {
        return service.findById(id);
    }
}
