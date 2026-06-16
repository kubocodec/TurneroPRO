package com.kubocode.turnero.dto;

public class TurnoDetalleDTO {
    private String numero;
    private String categoria;
    private String fechaLlamada;
    private String fechaFinAtencion;
    private long tiempoEsperaSegundos;
    private long tiempoAtencionSegundos;
    private String calificacion;
    private String observaciones;

    public TurnoDetalleDTO(String numero, String categoria, String fechaLlamada, String fechaFinAtencion, long tiempoEsperaSegundos, long tiempoAtencionSegundos, String calificacion, String observaciones) {
        this.numero = numero;
        this.categoria = categoria;
        this.fechaLlamada = fechaLlamada;
        this.fechaFinAtencion = fechaFinAtencion;
        this.tiempoEsperaSegundos = tiempoEsperaSegundos;
        this.tiempoAtencionSegundos = tiempoAtencionSegundos;
        this.calificacion = calificacion;
        this.observaciones = observaciones;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getFechaLlamada() { return fechaLlamada; }
    public void setFechaLlamada(String fechaLlamada) { this.fechaLlamada = fechaLlamada; }
    public String getFechaFinAtencion() { return fechaFinAtencion; }
    public void setFechaFinAtencion(String fechaFinAtencion) { this.fechaFinAtencion = fechaFinAtencion; }
    public long getTiempoEsperaSegundos() { return tiempoEsperaSegundos; }
    public void setTiempoEsperaSegundos(long tiempoEsperaSegundos) { this.tiempoEsperaSegundos = tiempoEsperaSegundos; }
    public long getTiempoAtencionSegundos() { return tiempoAtencionSegundos; }
    public void setTiempoAtencionSegundos(long tiempoAtencionSegundos) { this.tiempoAtencionSegundos = tiempoAtencionSegundos; }
    public String getCalificacion() { return calificacion; }
    public void setCalificacion(String calificacion) { this.calificacion = calificacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
