package com.kubocode.turnero.repository;

import com.kubocode.turnero.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    // Obtener todos los turnos con un estado específico
    List<Turno> findByEstado(String estado);

    // Obtener los turnos abiertos por categoría y preferencia, ordenados por fecha
    List<Turno> findByCategoriaIdAndPreferenteAndEstadoOrderByFechaCreacionAsc(Long categoriaId, boolean preferente, String estado);

    // Contar la cantidad de turnos abiertos por categoría
    Long countByCategoriaIdAndEstado(Long categoriaId, String estado);

    // Obtener los últimos turnos atendidos
    List<Turno> findTop5ByEstadoOrderByFechaCreacionDesc(String estado);

    Turno findTopByOrderByIdDesc();

    Turno findFirstByCategoriaIdAndPreferenteAndEstadoOrderByFechaCreacionAsc(Long categoriaId, boolean preferente, String estado);

    Turno findTopByCategoriaIdOrderByIdDesc(Long categoriaId);

    Turno findFirstByEstadoOrderByFechaActualizacionDesc(String estado);

    List<Turno> findTop5ByEstadoOrderByFechaActualizacionDesc(String estado);


}
