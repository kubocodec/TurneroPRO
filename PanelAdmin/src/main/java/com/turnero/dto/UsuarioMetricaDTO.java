package com.turnero.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioMetricaDTO {
    private Long usuarioId;
    private String nombreUsuario;
    private long turnosAtendidos;
    private double tiempoPromedioEsperaSegundos;
    private double tiempoPromedioAtencionSegundos;
    private String calificacionPromedio;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public long getTurnosAtendidos() { return turnosAtendidos; }
    public void setTurnosAtendidos(long turnosAtendidos) { this.turnosAtendidos = turnosAtendidos; }
    public double getTiempoPromedioEsperaSegundos() { return tiempoPromedioEsperaSegundos; }
    public void setTiempoPromedioEsperaSegundos(double tiempoPromedioEsperaSegundos) { this.tiempoPromedioEsperaSegundos = tiempoPromedioEsperaSegundos; }
    public double getTiempoPromedioAtencionSegundos() { return tiempoPromedioAtencionSegundos; }
    public void setTiempoPromedioAtencionSegundos(double tiempoPromedioAtencionSegundos) { this.tiempoPromedioAtencionSegundos = tiempoPromedioAtencionSegundos; }
    public String getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(String calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }
}
