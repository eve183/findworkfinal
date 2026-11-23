package com.findwork.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulacion")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulacion")
    private Integer idPostulacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_postulacion_usuario"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_postulacion_oferta"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private OfertaLaboral oferta;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_postulacion", updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaPostulacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "ENUM('pendiente','en_revision','aceptada','rechazada') DEFAULT 'pendiente'")
    private EstadoPostulacion estado;

    public Postulacion() {}

    public Integer getIdPostulacion() { return idPostulacion; }
    public void setIdPostulacion(Integer idPostulacion) { this.idPostulacion = idPostulacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @JsonProperty("usuarioId")
    public Integer getIdDelUsuario() { return usuario == null ? null : usuario.getIdUsuario(); }

    public OfertaLaboral getOferta() { return oferta; }
    public void setOferta(OfertaLaboral oferta) { this.oferta = oferta; }

    @JsonProperty("ofertaId")
    public Integer getIdDeLaOferta() { return oferta == null ? null : oferta.getIdOferta(); }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDateTime fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public EstadoPostulacion getEstado() { return estado; }
    public void setEstado(EstadoPostulacion estado) { this.estado = estado; }
}
