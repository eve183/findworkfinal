package com.findwork;

import com.findwork.entities.Categoria;
import com.findwork.entities.Empresa;
import com.findwork.entities.EstadoVerificacion;
import com.findwork.entities.OfertaLaboral;
import com.findwork.entities.RolUsuario;
import com.findwork.entities.Usuario;
import com.findwork.repositories.CategoriaRepository;
import com.findwork.repositories.EmpresaRepository;
import com.findwork.repositories.OfertaLaboralRepository;
import com.findwork.repositories.RolUsuarioRepository;
import com.findwork.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrudIntegrationTests {

    @Autowired private MockMvc mvc;
    @Autowired private RolUsuarioRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private OfertaLaboralRepository ofertaRepository;
    @Autowired private PasswordEncoder encoder;

    private Usuario admin;
    private Usuario empleador;
    private Usuario candidato;
    private Empresa empresa;
    private Categoria categoria;
    private OfertaLaboral oferta;

    @BeforeEach
    void prepararUsuarios() {
        admin = crearUsuario("ADMINISTRADOR", "admin-" + UUID.randomUUID() + "@example.test");
        empleador = crearUsuario("EMPLEADOR", "empleador-" + UUID.randomUUID() + "@example.test");
        candidato = crearUsuario("CANDIDATO", "candidato-" + UUID.randomUUID() + "@example.test");

        empresa = new Empresa();
        empresa.setEmpleador(empleador);
        empresa.setNit("NIT-" + UUID.randomUUID().toString().substring(0, 12));
        empresa.setRazonSocial("Empresa de prueba");
        empresa.setEstadoVerificacion(EstadoVerificacion.verificado);
        empresa = empresaRepository.save(empresa);

        categoria = new Categoria();
        categoria.setNombre("Categoría de prueba");
        categoria = categoriaRepository.save(categoria);

        oferta = new OfertaLaboral();
        oferta.setEmpresa(empresa);
        oferta.setCategoria(categoria);
        oferta.setTitulo("Oferta de prueba");
        oferta.setDescripcion("Descripción de prueba");
        oferta.setEstado(com.findwork.entities.EstadoOferta.activa);
        oferta = ofertaRepository.save(oferta);
    }

    private Usuario crearUsuario(String rolNombre, String correo) {
        RolUsuario rol = rolRepository.findByNombre(rolNombre).orElseThrow();
        Usuario u = new Usuario();
        u.setRol(rol);
        u.setCorreo(correo);
        u.setPassword(encoder.encode("clave123"));
        return usuarioRepository.save(u);
    }

    private String datosUsuario(String correo, String rolNombre) {
        RolUsuario rol = rolRepository.findByNombre(rolNombre).orElseThrow();
        return "{\"correo\":\"" + correo + "\",\"password\":\"clave-segura\",\"rol\":{\"idRol\":" + rol.getIdRol() + "}}";
    }

    @Test
    void rolesFijosEstanCreados() throws Exception {
        mvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/roles").with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItem("ADMINISTRADOR")))
                .andExpect(jsonPath("$[*].nombre", hasItem("EMPLEADOR")))
                .andExpect(jsonPath("$[*].nombre", hasItem("CANDIDATO")));
    }

    @Test
    void rolesNoSePuedenModificarPorApi() throws Exception {
        mvc.perform(post("/api/roles").with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"OTRO_ROL\"}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(put("/api/roles/{id}", admin.getRol().getIdRol()).with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"ROL_EDITADO\"}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/api/roles/{id}", admin.getRol().getIdRol()).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void correoDuplicadoDevuelve409() throws Exception {
        String correo = UUID.randomUUID() + "@example.test";
        String datos = datosUsuario(correo, "CANDIDATO");
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isConflict());
    }

    @Test
    void registroPublicoNoPermiteCrearAdministrador() throws Exception {
        String datos = datosUsuario(UUID.randomUUID() + "@example.test", "ADMINISTRADOR");
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuariosSoloAdministraAdminYUsuarioPropio() throws Exception {
        String correo = UUID.randomUUID() + "@example.test";
        String respuesta = mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datosUsuario(correo, "CANDIDATO")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(respuesta, "$.idUsuario")).intValue();

        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios/{id}", id)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/usuarios/{id}", id)
                        .with(httpBasic(correo, "clave-segura"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"editado@example.test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("editado@example.test"));
        mvc.perform(get("/api/usuarios/{id}", id).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("editado@example.test"));
        mvc.perform(delete("/api/usuarios/{id}", id).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoriasCrud() throws Exception {
        String crear = mvc.perform(post("/api/categorias").with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Tecnología\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idCategoria")).intValue();
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idCategoria", hasItem(id)));
        mvc.perform(put("/api/categorias/{id}", id).with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Diseño\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Diseño"));
        mvc.perform(delete("/api/categorias/{id}", id).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoriaInvalidaSeRechaza() throws Exception {
        mvc.perform(post("/api/categorias").with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void empresasCrud() throws Exception {
        String crear = mvc.perform(post("/api/empresas").with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nit\":\"123456\",\"razonSocial\":\"Nueva empresa\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idEmpresa")).intValue();
        mvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idEmpresa", not(hasItem(id))));
        mvc.perform(put("/api/empresas/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razonSocial\":\"Empresa editada\",\"estadoVerificacion\":\"verificado\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/empresas/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razonSocial\":\"Empresa editada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Empresa editada"));
        mvc.perform(delete("/api/empresas/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void ofertasCrud() throws Exception {
        String crear = mvc.perform(post("/api/ofertas").with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresa\":{\"idEmpresa\":" + empresa.getIdEmpresa() + "},\"categoria\":{\"idCategoria\":" + categoria.getIdCategoria() + "},\"titulo\":\"Desarrollador\",\"descripcion\":\"Vacante de prueba\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idOferta")).intValue();
        mvc.perform(get("/api/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idOferta", hasItem(id)));
        mvc.perform(put("/api/ofertas/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Analista\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Analista"));
        mvc.perform(delete("/api/ofertas/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void perfilesCrud() throws Exception {
        String crear = mvc.perform(post("/api/perfiles").with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Pérez\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idPerfil")).intValue();
        mvc.perform(get("/api/perfiles").with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idPerfil", hasItem(id)));
        mvc.perform(put("/api/perfiles/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"María\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María"));
        mvc.perform(delete("/api/perfiles/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void postulacionesCrud() throws Exception {
        String crear = mvc.perform(post("/api/postulaciones").with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":" + oferta.getIdOferta() + "},\"descripcion\":\"Me interesa\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idPostulacion")).intValue();
        mvc.perform(get("/api/postulaciones").with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idPostulacion", hasItem(id)));
        mvc.perform(put("/api/postulaciones/{id}", id).with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"aceptada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("aceptada"));
        mvc.perform(delete("/api/postulaciones/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void resenasCrud() throws Exception {
        String crear = mvc.perform(post("/api/resenas").with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresa\":{\"idEmpresa\":" + empresa.getIdEmpresa() + "},\"comentario\":\"Buena empresa\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idResena")).intValue();
        mvc.perform(get("/api/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idResena", hasItem(id)));
        mvc.perform(put("/api/resenas/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentario\":\"Muy buena empresa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentario").value("Muy buena empresa"));
        mvc.perform(delete("/api/resenas/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void notificacionesCrud() throws Exception {
        String crear = mvc.perform(post("/api/notificaciones").with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":{\"idUsuario\":" + candidato.getIdUsuario() + "},\"tipo\":\"aviso\",\"titulo\":\"Hola\",\"mensaje\":\"Mensaje de prueba\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(crear, "$.idNotificacion")).intValue();
        mvc.perform(get("/api/notificaciones").with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idNotificacion", hasItem(id)));
        mvc.perform(put("/api/notificaciones/{id}", id).with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Aviso actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Aviso actualizado"));
        mvc.perform(delete("/api/notificaciones/{id}", id).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void notificacionSePuedeMarcarComoLeida() throws Exception {
        String respuesta = mvc.perform(post("/api/notificaciones").with(httpBasic(admin.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":{\"idUsuario\":" + candidato.getIdUsuario() + "},\"tipo\":\"aviso\",\"titulo\":\"Hola\",\"mensaje\":\"Nuevo mensaje\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leida").value(false))
                .andReturn().getResponse().getContentAsString();
        int id = ((Number) JsonPath.read(respuesta, "$.idNotificacion")).intValue();
        mvc.perform(patch("/api/notificaciones/{id}/leer", id).with(httpBasic(candidato.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void relacionesSeDevuelvenPorIdSinExponerPassword() throws Exception {
        mvc.perform(get("/api/usuarios/{id}", candidato.getIdUsuario()).with(httpBasic(admin.getCorreo(), "clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolId").value(candidato.getRol().getIdRol()))
                .andExpect(jsonPath("$.rol").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/ofertas/{id}", oferta.getIdOferta()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(empresa.getIdEmpresa()))
                .andExpect(jsonPath("$.categoriaId").value(categoria.getIdCategoria()));
    }

    @Test
    void recursoInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/categorias/999999"))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/ofertas/999999").with(httpBasic(empleador.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"No existe\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/postulaciones").with(httpBasic(candidato.getCorreo(), "clave123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oferta\":{\"idOferta\":999999},\"descripcion\":\"Me interesa\"}"))
                .andExpect(status().isNotFound());
    }
}
