package com.findwork.security.dto;

/** Respuesta que recibe el cliente tras login/registro exitoso. */
public class AuthResponse {

    private String token;
    private String correo;
    private String rol;
    private Integer idUsuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, String correo, String rol, Integer idUsuario) {
        this.token = token;
        this.correo = correo;
        this.rol = rol;
        this.idUsuario = idUsuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}
