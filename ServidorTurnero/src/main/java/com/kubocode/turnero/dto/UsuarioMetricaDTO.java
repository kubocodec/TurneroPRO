package com.kubocode.turnero.dto;

public class UsuarioMetricaDTO {
    private Long usuarioId;
    private String nombreUsuario;
    private long turnosAtendidos;
    private double tiempoPromedioEsperaSegundos;
    private double tiempoPromedioAtencionSegundos;
    private String calificacionPromedio;

    public UsuarioMetricaDTO(Long usuarioId, String nombreUsuario, long turnosAtendidos, double tiempoPromedioEsperaSegundos, double tiempoPromedioAtencionSegundos, String calificacionPromedio) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.turnosAtendidos = turnosAtendidos;
        this.tiempoPromedioEsperaSegundos = tiempoPromedioEsperaSegundos;
        this.tiempoPromedioAtencionSegundos = tiempoPromedioAtencionSegundos;
        this.calificacionPromedio = calificacionPromedio;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public long getTurnosAtendidos() {
        return turnosAtendidos;
    }

    public void setTurnosAtendidos(long turnosAtendidos) {
        this.turnosAtendidos = turnosAtendidos;
    }

    public double getTiempoPromedioEsperaSegundos() {
        return tiempoPromedioEsperaSegundos;
    }

    public void setTiempoPromedioEsperaSegundos(double tiempoPromedioEsperaSegundos) {
        this.tiempoPromedioEsperaSegundos = tiempoPromedioEsperaSegundos;
    }

    public double getTiempoPromedioAtencionSegundos() {
        return tiempoPromedioAtencionSegundos;
    }

    public void setTiempoPromedioAtencionSegundos(double tiempoPromedioAtencionSegundos) {
        this.tiempoPromedioAtencionSegundos = tiempoPromedioAtencionSegundos;
    }

    public String getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(String calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
}
