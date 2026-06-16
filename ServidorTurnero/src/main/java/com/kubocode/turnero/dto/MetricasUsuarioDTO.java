package com.kubocode.turnero.dto;

import java.util.Map;

public class MetricasUsuarioDTO {
    private long totalTurnosAtendidos;
    private double tiempoPromedioAtencionSegundos;
    private double tiempoPromedioEsperaSegundos;
    private String calificacionPromedio;
    private Map<String, Long> distribucionCalificaciones;
    private long totalRellamadas;
    private java.util.List<TurnoDetalleDTO> detalleTurnos;

    // Getters y Setters
    public long getTotalTurnosAtendidos() {
        return totalTurnosAtendidos;
    }

    public void setTotalTurnosAtendidos(long totalTurnosAtendidos) {
        this.totalTurnosAtendidos = totalTurnosAtendidos;
    }

    public double getTiempoPromedioAtencionSegundos() {
        return tiempoPromedioAtencionSegundos;
    }

    public void setTiempoPromedioAtencionSegundos(double tiempoPromedioAtencionSegundos) {
        this.tiempoPromedioAtencionSegundos = tiempoPromedioAtencionSegundos;
    }

    public double getTiempoPromedioEsperaSegundos() {
        return tiempoPromedioEsperaSegundos;
    }

    public void setTiempoPromedioEsperaSegundos(double tiempoPromedioEsperaSegundos) {
        this.tiempoPromedioEsperaSegundos = tiempoPromedioEsperaSegundos;
    }

    public String getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(String calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    public Map<String, Long> getDistribucionCalificaciones() {
        return distribucionCalificaciones;
    }

    public void setDistribucionCalificaciones(Map<String, Long> distribucionCalificaciones) {
        this.distribucionCalificaciones = distribucionCalificaciones;
    }

    public long getTotalRellamadas() {
        return totalRellamadas;
    }

    public void setTotalRellamadas(long totalRellamadas) {
        this.totalRellamadas = totalRellamadas;
    }

    public java.util.List<TurnoDetalleDTO> getDetalleTurnos() {
        return detalleTurnos;
    }

    public void setDetalleTurnos(java.util.List<TurnoDetalleDTO> detalleTurnos) {
        this.detalleTurnos = detalleTurnos;
    }
}
