package com.kubocode.turnero.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Turno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String estado; // "abierto", "atendido"

    private Boolean preferente;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private Integer puesto;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario atendidoPor;

    private Integer cantidadLlamadas = 1;

    public Turno() {
    }

    public Turno(Long id, Categoria categoria, String estado, LocalDateTime fechaActualizacion, LocalDateTime fechaCreacion, String numero, Boolean preferente, Integer puesto, Usuario atendidoPor) {
        this.id = id;
        this.categoria = categoria;
        this.estado = estado;
        this.fechaActualizacion = fechaActualizacion;
        this.fechaCreacion = fechaCreacion;
        this.numero = numero;
        this.preferente = preferente;
        this.puesto = puesto;
        this.atendidoPor = atendidoPor;
    }

    public Usuario getAtendidoPor() {
        return atendidoPor;
    }

    public void setAtendidoPor(Usuario atendidoPor) {
        this.atendidoPor = atendidoPor;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Boolean getPreferente() {
        return preferente;
    }

    public void setPreferente(Boolean preferente) {
        this.preferente = preferente;
    }

    public Integer getPuesto() {
        return puesto;
    }

    public void setPuesto(Integer puesto) {
        this.puesto = puesto;
    }

    public Integer getCantidadLlamadas() {
        return cantidadLlamadas;
    }

    public void setCantidadLlamadas(Integer cantidadLlamadas) {
        this.cantidadLlamadas = cantidadLlamadas;
    }
}


