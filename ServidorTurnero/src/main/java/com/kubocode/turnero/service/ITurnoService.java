package com.kubocode.turnero.service;


import com.kubocode.turnero.model.Turno;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

public interface ITurnoService {
    Turno guardarTurno(Turno turno);
    List<Turno> obtenerTurnosAbiertosPorPreferencia(boolean preferente);
    Turno avanzarSiguienteTurno(Long categoriaId, boolean preferente);
    Map<String, Long> contarTurnosPorCategoria();
    List<Turno> obtenerUltimosTurnosAtendidos(int limite);
    Turno obtenerUltimoTurno();
    Turno cerrarTurno(Long categoriaId, boolean preferente, Long usuarioId, Integer puesto);
    Turno obtenerUltimoTurnoProcesado();
    List<Turno> obtenerUltimosTurnosAtendidos();
    void reiniciarTurnos();
    Turno rellamarTurno(Long id);


}
