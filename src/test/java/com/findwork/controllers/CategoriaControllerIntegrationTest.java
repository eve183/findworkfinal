package com.findwork.controllers;

import com.findwork.entities.Categoria;
import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.CategoriaRepository;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoriaControllerIntegrationTest {

    private static final String BASE_URL = "/api/categorias";
    private static final String CLAVE = "clave123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        RolUsuario rolAdmin = rolUsuarioRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        admin = new Usuario();
        admin.setCorreo("admin-categoria@test.com");
        admin.setPassword(passwordEncoder.encode(CLAVE));
        admin.setRol(rolAdmin);
        admin = usuarioRepository.save(admin);
    }

    private Categoria crearCategoriaEnBD(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion("Descripción de " + nombre);
        return categoriaRepository.save(categoria);
    }

    @Test
    void deberiaCrearCategoriaComoAdmin() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic(admin.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tecnología\",\"descripcion\":\"Ofertas de TI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").isNumber())
                .andExpect(jsonPath("$.nombre").value("Tecnología"))
                .andExpect(jsonPath("$.descripcion").value("Ofertas de TI"));
    }

    @Test
    void deberiaRechazarCreacionSinAutenticacion() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tecnología\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberiaListarCategoriasSinAutenticacion() throws Exception {
        Categoria guardada = crearCategoriaEnBD("Salud");

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idCategoria", hasItem(guardada.getIdCategoria())))
                .andExpect(jsonPath("$[*].nombre", hasItem("Salud")));
    }

    @Test
    void deberiaObtenerCategoriaPorId() throws Exception {
        Categoria guardada = crearCategoriaEnBD("Educación");

        mockMvc.perform(get(BASE_URL + "/" + guardada.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(guardada.getIdCategoria()))
                .andExpect(jsonPath("$.nombre").value("Educación"));
    }

    @Test
    void deberiaActualizarCategoriaComoAdmin() throws Exception {
        Categoria guardada = crearCategoriaEnBD("Ventas");

        mockMvc.perform(put(BASE_URL + "/" + guardada.getIdCategoria())
                        .with(httpBasic(admin.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Comercial\",\"descripcion\":\"Área comercial\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(guardada.getIdCategoria()))
                .andExpect(jsonPath("$.nombre").value("Comercial"))
                .andExpect(jsonPath("$.descripcion").value("Área comercial"));
    }

    @Test
    void deberiaEliminarCategoriaComoAdmin() throws Exception {
        Categoria guardada = crearCategoriaEnBD("Temporal");
        Integer id = guardada.getIdCategoria();

        mockMvc.perform(delete(BASE_URL + "/" + id).with(httpBasic(admin.getCorreo(), CLAVE)))
                .andExpect(status().isNoContent());

        assertFalse(categoriaRepository.existsById(id));
    }

    @Test
    void deberiaRechazarCategoriaSinNombre() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic(admin.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").isNotEmpty());
    }

    @Test
    void deberiaDar404CuandoCategoriaNoExiste() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound());
    }
}
