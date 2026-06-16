package com.turnero;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Turno {
    private Long id;
    private String numero;
    private Categoria categoria;
    private String calificacion;

    // Getters obligatorios
    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }
}
