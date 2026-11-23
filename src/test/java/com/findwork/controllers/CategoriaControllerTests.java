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

import com.findwork.entities.Categoria;
import com.findwork.exceptions.RecursoNoEncontradoException;
import com.findwork.security.JwtAuthenticationFilter;
import com.findwork.services.CategoriaService;

@WebMvcTest(controllers = CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CategoriaService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Categoria categoria;

    @BeforeEach
    void setup() {
        categoria = Categoria.builder()
                .idCategoria(1)
                .nombre("Tecnología")
                .descripcion("Ofertas de tecnología")
                .build();
    }

    @DisplayName("Test para listar las categorías")
    @Test
    void testListarCategorias() throws Exception {
        // given
        given(service.findAll()).willReturn(List.of(categoria));

        // when / then
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCategoria").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Tecnología"))
                .andExpect(jsonPath("$[0].descripcion").value("Ofertas de tecnología"));
    }

    @DisplayName("Test para obtener una categoría por ID")
    @Test
    void testObtenerCategoria() throws Exception {
        // given
        given(service.findById(1)).willReturn(categoria);

        // when / then
        mvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Tecnología"));
    }

    @DisplayName("Test para crear una categoría")
    @Test
    void testCrearCategoria() throws Exception {
        // given
        given(service.save(any(Categoria.class))).willReturn(categoria);

        // when / then
        mvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tecnología\",\"descripcion\":\"Ofertas de tecnología\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Tecnología"));
    }

    @DisplayName("Test para actualizar una categoría")
    @Test
    void testActualizarCategoria() throws Exception {
        // given
        given(service.update(eq(1), any(Categoria.class))).willReturn(categoria);

        // when / then
        mvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Diseño\",\"descripcion\":\"Nueva descripción\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1));
    }

    @DisplayName("Test para eliminar una categoría")
    @Test
    void testEliminarCategoria() throws Exception {
        // when / then
        mvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1);
    }

    @DisplayName("Test para rechazar una categoría sin nombre")
    @Test
    void testCrearCategoriaInvalida() throws Exception {
        // when / then
        mvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").isNotEmpty());

        verify(service, never()).save(any(Categoria.class));
    }

    @DisplayName("Test para retornar 404 cuando la categoría no existe")
    @Test
    void testObtenerCategoriaInexistente() throws Exception {
        // given
        given(service.findById(999)).willThrow(
                new RecursoNoEncontradoException("Categoría no encontrada con id: 999"));

        // when / then
        mvc.perform(get("/api/categorias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Categoría no encontrada con id: 999"));
    }
}
