package com.findwork.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UsuarioControllerIntegrationTest {
    // Cambia esto si tu @RequestMapping tiene otra ruta (ej: "/usuarios")
    private static final String BASE_URL = "/api/usuarios";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RolUsuario rolGuardado;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        rolGuardado = rolUsuarioRepository.findByNombre("CANDIDATO").orElseThrow();

        RolUsuario rolAdmin = rolUsuarioRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        admin = new Usuario();
        admin.setCorreo("admin@test.com");
        admin.setPassword(passwordEncoder.encode("clave123"));
        admin.setRol(rolAdmin);
        admin = usuarioRepository.save(admin);
    }

    @Test
    void deberiaCrearUsuario() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "correo", "juan@findwork.com",
                "password", "Password123",
                "rol", Map.of("idRol", rolGuardado.getIdRol())
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is2xxSuccessful()) // Acepta 200 OK o 201 Created
                .andExpect(jsonPath("$.idUsuario").isNumber())
                .andExpect(jsonPath("$.correo").value("juan@findwork.com"))
                .andExpect(jsonPath("$.rolId").value(rolGuardado.getIdRol()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.rol").doesNotExist());
    }

    @Test
    void deberiaObtenerUsuarioPorId() throws Exception {
        Usuario guardado = crearUsuarioEnBD("ana@findwork.com", "Secreto123");

        mockMvc.perform(get(BASE_URL + "/" + guardado.getIdUsuario())
                        .with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(guardado.getIdUsuario()))
                .andExpect(jsonPath("$.correo").value("ana@findwork.com"))
                .andExpect(jsonPath("$.rolId").value(rolGuardado.getIdRol()));
    }

    @Test
    void deberiaListarUsuarios() throws Exception {
        crearUsuarioEnBD("user1@findwork.com", "123456");
        crearUsuarioEnBD("user2@findwork.com", "123456");

        mockMvc.perform(get(BASE_URL)
                        .with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].correo", hasItems("user1@findwork.com", "user2@findwork.com")));
    }

    @Test
    void deberiaActualizarUsuario() throws Exception {
        Usuario guardado = crearUsuarioEnBD("viejo@findwork.com", "123456");

        Map<String, Object> updateBody = Map.of(
                "correo", "actualizado@findwork.com",
                "password", "NuevaClave123",
                "rol", Map.of("idRol", rolGuardado.getIdRol())
        );

        mockMvc.perform(put(BASE_URL + "/" + guardado.getIdUsuario())
                        .with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("actualizado@findwork.com"));
    }

    @Test
    void deberiaEliminarUsuario() throws Exception {
        Usuario guardado = crearUsuarioEnBD("borrar@findwork.com", "123456");

        mockMvc.perform(delete(BASE_URL + "/" + guardado.getIdUsuario())
                        .with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().is2xxSuccessful());

        assertFalse(usuarioRepository.existsById(guardado.getIdUsuario()));
    }

    // Método auxiliar para preparar datos directo en BD
    private Usuario crearUsuarioEnBD(String correo, String password) {
        Usuario u = new Usuario();
        u.setCorreo(correo);
        u.setPassword(password);
        u.setRol(rolGuardado);
        return usuarioRepository.save(u);
    }
}
