package com.findwork;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthJwtIntegrationTests {

    @Autowired private MockMvc mvc;

    private String correo() {
        return "jwt-" + UUID.randomUUID() + "@example.test";
    }

    private String cuerpo(String correo, String contrasena, String rol) {
        return "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\",\"rol\":\"" + rol + "\"}";
    }

    @Test
    void registroDevuelveTokenYAccesoProtegidoConBearer() throws Exception {
        String correo = correo();
        String respuesta = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(correo, "clave123", "CANDIDATO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.correo").value(correo))
                .andExpect(jsonPath("$.rol").value("CANDIDATO"))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(respuesta, "$.token");

        mvc.perform(get("/api/perfiles"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginValidoDevuelveTokenYClaveInvalidaDevuelve401() throws Exception {
        String correo = correo();
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(correo, "clave123", "EMPLEADOR")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("EMPLEADOR"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroNoPermiteAdministradorNiCorreoDuplicado() throws Exception {
        String correo = correo();
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(correo, "clave123", "ADMINISTRADOR")))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(correo, "clave123", "CANDIDATO")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(correo, "clave123", "CANDIDATO")))
                .andExpect(status().isConflict());
    }
}
