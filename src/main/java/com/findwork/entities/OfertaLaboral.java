package com.findwork.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "oferta_laboral")
public class OfertaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Integer idOferta;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oferta_empresa"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Empresa empresa;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oferta_categoria"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Categoria categoria;

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "requisitos", columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "salario", precision = 12, scale = 2)
    private BigDecimal salario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion", columnDefinition = "ENUM('termino_fijo','termino_indefinido','prestacion_servicios','obra_labor','otro')")
    private TipoVinculacion tipoVinculacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_trabajo", columnDefinition = "ENUM('presencial','remoto','hibrido')")
    private ModalidadTrabajo modalidadTrabajo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_experiencia", columnDefinition = "ENUM('sin_experiencia','junior','medio','senior')")
    private NivelExperiencia nivelExperiencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "jornada_laboral", columnDefinition = "ENUM('tiempo_completo','medio_tiempo','por_horas')")
    private JornadaLaboral jornadaLaboral;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "fecha_publicacion", updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "ENUM('activa','cerrada','pausada') DEFAULT 'activa'")
    private EstadoOferta estado;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Postulacion> postulaciones;

    public OfertaLaboral() {}

    public Integer getIdOferta() { return idOferta; }
    public void setIdOferta(Integer idOferta) { this.idOferta = idOferta; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    @JsonProperty("empresaId")
    public Integer getIdDeLaEmpresa() { return empresa == null ? null : empresa.getIdEmpresa(); }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    @JsonProperty("categoriaId")
    public Integer getIdDeLaCategoria() { return categoria == null ? null : categoria.getIdCategoria(); }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }

    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }

    public TipoVinculacion getTipoVinculacion() { return tipoVinculacion; }
    public void setTipoVinculacion(TipoVinculacion tipoVinculacion) { this.tipoVinculacion = tipoVinculacion; }

    public ModalidadTrabajo getModalidadTrabajo() { return modalidadTrabajo; }
    public void setModalidadTrabajo(ModalidadTrabajo modalidadTrabajo) { this.modalidadTrabajo = modalidadTrabajo; }

    public NivelExperiencia getNivelExperiencia() { return nivelExperiencia; }
    public void setNivelExperiencia(NivelExperiencia nivelExperiencia) { this.nivelExperiencia = nivelExperiencia; }

    public JornadaLaboral getJornadaLaboral() { return jornadaLaboral; }
    public void setJornadaLaboral(JornadaLaboral jornadaLaboral) { this.jornadaLaboral = jornadaLaboral; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public EstadoOferta getEstado() { return estado; }
    public void setEstado(EstadoOferta estado) { this.estado = estado; }

    public List<Postulacion> getPostulaciones() { return postulaciones; }
    public void setPostulaciones(List<Postulacion> postulaciones) { this.postulaciones = postulaciones; }
}
