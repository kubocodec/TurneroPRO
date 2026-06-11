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

}
