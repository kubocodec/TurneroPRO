package com.kubocode.turnero.dto;

import java.util.List;

public class MetricasGeneralesDTO {
    private long totalTurnosDelDia;
    private long turnosAtendidos;
    private long turnosPendientes;
    private double tiempoPromedioEsperaGeneral;
    private double tiempoPromedioAtencionGeneral;
    private List<UsuarioMetricaDTO> metricasPorUsuario;

    public long getTotalTurnosDelDia() {
        return totalTurnosDelDia;
    }

    public void setTotalTurnosDelDia(long totalTurnosDelDia) {
        this.totalTurnosDelDia = totalTurnosDelDia;
    }

    public long getTurnosAtendidos() {
        return turnosAtendidos;
    }

    public void setTurnosAtendidos(long turnosAtendidos) {
        this.turnosAtendidos = turnosAtendidos;
    }

    public long getTurnosPendientes() {
        return turnosPendientes;
    }

    public void setTurnosPendientes(long turnosPendientes) {
        this.turnosPendientes = turnosPendientes;
    }

    public double getTiempoPromedioEsperaGeneral() {
        return tiempoPromedioEsperaGeneral;
    }

    public void setTiempoPromedioEsperaGeneral(double tiempoPromedioEsperaGeneral) {
        this.tiempoPromedioEsperaGeneral = tiempoPromedioEsperaGeneral;
    }

    public double getTiempoPromedioAtencionGeneral() {
        return tiempoPromedioAtencionGeneral;
    }

    public void setTiempoPromedioAtencionGeneral(double tiempoPromedioAtencionGeneral) {
        this.tiempoPromedioAtencionGeneral = tiempoPromedioAtencionGeneral;
    }

    public List<UsuarioMetricaDTO> getMetricasPorUsuario() {
        return metricasPorUsuario;
    }

    public void setMetricasPorUsuario(List<UsuarioMetricaDTO> metricasPorUsuario) {
        this.metricasPorUsuario = metricasPorUsuario;
    }
}
