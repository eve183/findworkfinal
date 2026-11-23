package com.findwork.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empleador_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_empresa_usuario"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Usuario empleador;

    @NotBlank(message = "El NIT es obligatorio")
    @Column(name = "nit", nullable = false, length = 30)
    private String nit;

    @NotBlank(message = "La razón social es obligatoria")
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "sitio_web", length = 500)
    private String sitioWeb;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", columnDefinition = "ENUM('pendiente','verificado','rechazado') DEFAULT 'pendiente'")
    private EstadoVerificacion estadoVerificacion;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OfertaLaboral> ofertas;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Resena> resenas;

    public Empresa() {}

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Usuario getEmpleador() { return empleador; }
    public void setEmpleador(Usuario empleador) { this.empleador = empleador; }

    @JsonProperty("empleadorId")
    public Integer getIdDelEmpleador() { return empleador == null ? null : empleador.getIdUsuario(); }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getSitioWeb() { return sitioWeb; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoVerificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public List<OfertaLaboral> getOfertas() { return ofertas; }
    public void setOfertas(List<OfertaLaboral> ofertas) { this.ofertas = ofertas; }

    public List<Resena> getResenas() { return resenas; }
    public void setResenas(List<Resena> resenas) { this.resenas = resenas; }
}
