package com.findwork.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.security.JwtAuthenticationFilter;
import com.findwork.services.UsuarioService;

@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        RolUsuario rol = RolUsuario.builder()
                .idRol(3)
                .nombre("CANDIDATO")
                .build();
        usuario = Usuario.builder()
                .idUsuario(1)
                .correo("candidato@example.test")
                .password("hash")
                .rol(rol)
                .build();
    }

    @DisplayName("Test para listar los usuarios")
    @Test
    void testListarUsuarios() throws Exception {
        // given
        given(service.findAll()).willReturn(List.of(usuario));

        // when / then
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(1))
                .andExpect(jsonPath("$[0].correo").value("candidato@example.test"))
                .andExpect(jsonPath("$[0].rolId").value(3))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].rol").doesNotExist());
    }

    @DisplayName("Test para obtener un usuario por ID")
    @Test
    void testObtenerUsuario() throws Exception {
        // given
        given(service.findById(1)).willReturn(usuario);

        // when / then
        mvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.correo").value("candidato@example.test"));
    }

    @DisplayName("Test para crear un usuario")
    @Test
    void testCrearUsuario() throws Exception {
        // given
        given(service.save(any(Usuario.class))).willReturn(usuario);

        // when / then
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"candidato@example.test\",\"password\":\"clave123\",\"rol\":{\"idRol\":3}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.correo").value("candidato@example.test"))
                .andExpect(jsonPath("$.rolId").value(3))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.rol").doesNotExist());
    }

    @DisplayName("Test para actualizar un usuario")
    @Test
    void testActualizarUsuario() throws Exception {
        // given
        given(service.update(eq(1), any(Usuario.class))).willReturn(usuario);

        // when / then
        mvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"nuevo@example.test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1));
    }

    @DisplayName("Test para eliminar un usuario")
    @Test
    void testEliminarUsuario() throws Exception {
        // when / then
        mvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1);
    }

    @DisplayName("Test para rechazar un usuario con correo inválido")
    @Test
    void testCrearUsuarioInvalido() throws Exception {
        // when / then
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"no-es-un-correo\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any(Usuario.class));
    }

    @DisplayName("Test para retornar 404 cuando el usuario no existe")
    @Test
    void testObtenerUsuarioInexistente() throws Exception {
        // given
        given(service.findById(999)).willThrow(
                new RecursoNoEncontradoException("Usuario no encontrado con id: 999"));

        // when / then
        mvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado con id: 999"));
    }
}
