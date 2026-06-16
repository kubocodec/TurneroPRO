package com.kubocode.turnero.controller;

import com.kubocode.turnero.model.Turno;
import com.kubocode.turnero.service.ITurnoService;
import com.kubocode.turnero.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    @Autowired
    private ITurnoService turnoService;

    @Autowired
    private com.kubocode.turnero.service.BotoneraSerialService botoneraSerialService;

    @PostMapping
    public Turno crearTurno(@RequestBody Turno turno) {
        return turnoService.guardarTurno(turno);
    }

    @GetMapping("/abiertos")
    public List<Turno> turnosPorPreferencia(@RequestParam boolean preferente) {
        return turnoService.obtenerTurnosAbiertosPorPreferencia(preferente);
    }

    @PostMapping("/siguiente")
    public Turno siguienteTurno(@RequestParam Long categoriaId, @RequestParam boolean preferente) {
        return turnoService.avanzarSiguienteTurno(categoriaId, preferente);
    }

    @GetMapping("/conteo")
    public Map<String, Long> conteoPorCategoria() {
        return turnoService.contarTurnosPorCategoria();
    }

    @GetMapping("/ultimo")
    public Turno obtenerUltimoTurno() {
        return turnoService.obtenerUltimoTurnoProcesado();
    }

    @PostMapping("/cerrar")
    public Turno cerrarTurno(@RequestBody Map<String, Object> datos) {
        Long categoriaId = Long.parseLong(datos.get("categoriaId").toString());
        boolean preferente = Boolean.parseBoolean(datos.get("preferente").toString());
        Long usuarioId = Long.parseLong(datos.get("usuarioId").toString());
        Integer puesto = Integer.parseInt(datos.get("puesto").toString()); // ✅ CAMBIO AQUÍ

        return turnoService.cerrarTurno(categoriaId, preferente, usuarioId, puesto);
    }

    @GetMapping("/ultimos")
    public List<Turno> ultimosTurnosAtendidos() {
        return turnoService.obtenerUltimosTurnosAtendidos();
    }

    @PostMapping("/reiniciar")
    public ResponseEntity<String> reiniciarTurnos() {
        turnoService.reiniciarTurnos();
        return ResponseEntity.ok("Los turnos han sido reiniciados a 0");
    }

    @PostMapping("/{id}/rellamar")
    public Turno rellamarTurno(@PathVariable("id") Long id) {
        return turnoService.rellamarTurno(id);
    }

    @PostMapping("/{id}/finalizar")
    public Turno finalizarAtencion(@PathVariable("id") Long id, @RequestBody Map<String, String> datos) {
        String calificacion = datos.get("calificacion");
        String observaciones = datos.get("observaciones");
        return turnoService.finalizarAtencion(id, calificacion, observaciones);
    }

    @GetMapping("/metricas/usuario/{id}")
    public com.kubocode.turnero.dto.MetricasUsuarioDTO metricasUsuario(
            @PathVariable("id") Long usuarioId,
            @RequestParam("start") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
            @RequestParam("end") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end) {
        return turnoService.obtenerMetricasUsuario(usuarioId, start, end);
    }

    @GetMapping("/metricas/general")
    public com.kubocode.turnero.dto.MetricasGeneralesDTO metricasGenerales(
            @RequestParam("start") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
            @RequestParam("end") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end) {
        return turnoService.obtenerMetricasGenerales(start, end);
    }

    @PostMapping("/{id}/transferir")
    public Turno transferirTurno(@PathVariable("id") Long id, @RequestParam("nuevaCategoriaId") Long nuevaCategoriaId, @RequestParam(value = "puesto", required = false) Integer puesto) {
        return turnoService.transferirTurno(id, nuevaCategoriaId, puesto);
    }

    @PutMapping("/botonera/puesto/{puesto}")
    public ResponseEntity<Void> actualizarPuestoBotonera(@PathVariable("puesto") Integer puesto) {
        botoneraSerialService.setPuestoId(puesto);
        return ResponseEntity.ok().build();
    }
}
