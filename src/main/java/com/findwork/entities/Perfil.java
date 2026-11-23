package com.findwork.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer idPerfil;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_perfil_usuario"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Usuario usuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "ubicacion_actual", length = 150)
    private String ubicacionActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta", columnDefinition = "ENUM('activo','inactivo','suspendido') DEFAULT 'activo'")
    private EstadoCuenta estadoCuenta;

    @Column(name = "hoja_de_vida_nombre", length = 255)
    private String hojaDeVidaNombre;

    @Column(name = "hoja_de_vida_url", length = 500)
    private String hojaDeVidaUrl;

    public Perfil() {}

    public Integer getIdPerfil() { return idPerfil; }
    public void setIdPerfil(Integer idPerfil) { this.idPerfil = idPerfil; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @JsonProperty("usuarioId")
    public Integer getIdDelUsuario() { return usuario == null ? null : usuario.getIdUsuario(); }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getUbicacionActual() { return ubicacionActual; }
    public void setUbicacionActual(String ubicacionActual) { this.ubicacionActual = ubicacionActual; }

    public EstadoCuenta getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuenta estadoCuenta) { this.estadoCuenta = estadoCuenta; }

    public String getHojaDeVidaNombre() { return hojaDeVidaNombre; }
    public void setHojaDeVidaNombre(String hojaDeVidaNombre) { this.hojaDeVidaNombre = hojaDeVidaNombre; }

    public String getHojaDeVidaUrl() { return hojaDeVidaUrl; }
    public void setHojaDeVidaUrl(String hojaDeVidaUrl) { this.hojaDeVidaUrl = hojaDeVidaUrl; }
}
