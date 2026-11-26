package com.findwork.controllers;

import com.findwork.entities.Categoria;
import com.findwork.entities.Empresa;
import com.findwork.entities.EstadoOferta;
import com.findwork.entities.EstadoPostulacion;
import com.findwork.entities.EstadoVerificacion;
import com.findwork.entities.OfertaLaboral;
import com.findwork.entities.Postulacion;
import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.CategoriaRepository;
import com.findwork.repositories.EmpresaRepository;
import com.findwork.repositories.OfertaLaboralRepository;
import com.findwork.repositories.PostulacionRepository;
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

import java.util.UUID;

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
class PostulacionControllerIntegrationTest {

    private static final String BASE_URL = "/api/postulaciones";
    private static final String CLAVE = "clave123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OfertaLaboralRepository ofertaRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario empleador;
    private Usuario candidato;
    private OfertaLaboral oferta;

    @BeforeEach
    void setUp() {
        empleador = crearUsuario("empleador-postulacion@test.com", "EMPLEADOR");
        candidato = crearUsuario("candidato-postulacion@test.com", "CANDIDATO");

        Empresa empresa = new Empresa();
        empresa.setEmpleador(empleador);
        empresa.setNit("NIT-" + UUID.randomUUID().toString().substring(0, 10));
        empresa.setRazonSocial("Empresa de prueba");
        empresa.setEstadoVerificacion(EstadoVerificacion.verificado);
        empresa = empresaRepository.save(empresa);

        Categoria categoria = new Categoria();
        categoria.setNombre("Tecnología");
        categoria = categoriaRepository.save(categoria);

        oferta = new OfertaLaboral();
        oferta.setEmpresa(empresa);
        oferta.setCategoria(categoria);
        oferta.setTitulo("Desarrollador");
        oferta.setDescripcion("Vacante de prueba");
        oferta.setEstado(EstadoOferta.activa);
        oferta = ofertaRepository.save(oferta);
    }

    private Usuario crearUsuario(String correo, String rolNombre) {
        RolUsuario rol = rolUsuarioRepository.findByNombre(rolNombre).orElseThrow();
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(CLAVE));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    private Postulacion crearPostulacionEnBD(String descripcion) {
        Postulacion postulacion = new Postulacion();
        postulacion.setUsuario(candidato);
        postulacion.setOferta(oferta);
        postulacion.setDescripcion(descripcion);
        postulacion.setEstado(EstadoPostulacion.pendiente);
        return postulacionRepository.save(postulacion);
    }

    @Test
    void deberiaCrearPostulacionComoCandidato() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic(candidato.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":" + oferta.getIdOferta()
                                + "},\"descripcion\":\"Me interesa el cargo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPostulacion").isNumber())
                .andExpect(jsonPath("$.descripcion").value("Me interesa el cargo"))
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andExpect(jsonPath("$.usuarioId").value(candidato.getIdUsuario()))
                .andExpect(jsonPath("$.ofertaId").value(oferta.getIdOferta()));
    }

    @Test
    void deberiaRechazarCreacionSinAutenticacion() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":" + oferta.getIdOferta() + "}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberiaRechazarPostulacionDeNoCandidato() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic(empleador.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":" + oferta.getIdOferta() + "}}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaDar404CuandoLaOfertaNoExiste() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic(candidato.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":999999},\"descripcion\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deberiaListarPostulacionesDelCandidato() throws Exception {
        Postulacion guardada = crearPostulacionEnBD("Primera postulación");

        mockMvc.perform(get(BASE_URL).with(httpBasic(candidato.getCorreo(), CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idPostulacion", hasItem(guardada.getIdPostulacion())));
    }

    @Test
    void deberiaListarPostulacionesRecibidasPorEmpleador() throws Exception {
        Postulacion guardada = crearPostulacionEnBD("Recibida por la empresa");

        mockMvc.perform(get(BASE_URL).with(httpBasic(empleador.getCorreo(), CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idPostulacion", hasItem(guardada.getIdPostulacion())));
    }

    @Test
    void deberiaPermitirAlEmpleadorActualizarEstado() throws Exception {
        Postulacion guardada = crearPostulacionEnBD("En revisión");

        mockMvc.perform(put(BASE_URL + "/" + guardada.getIdPostulacion())
                        .with(httpBasic(empleador.getCorreo(), CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"aceptada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("aceptada"));
    }

    @Test
    void deberiaEliminarPostulacionComoCandidato() throws Exception {
        Postulacion guardada = crearPostulacionEnBD("Para eliminar");
        Integer id = guardada.getIdPostulacion();

        mockMvc.perform(delete(BASE_URL + "/" + id)
                        .with(httpBasic(candidato.getCorreo(), CLAVE)))
                .andExpect(status().isNoContent());

        assertFalse(postulacionRepository.existsById(id));
    }

    @Test
    void deberiaDar404CuandoPostulacionNoExiste() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999")
                        .with(httpBasic(candidato.getCorreo(), CLAVE)))
                .andExpect(status().isNotFound());
    }
}
