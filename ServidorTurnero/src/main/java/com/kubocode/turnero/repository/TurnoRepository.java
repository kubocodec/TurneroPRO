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
    List<Turno> findTop5ByEstadoAndArchivadoFalseOrderByFechaCreacionDesc(String estado);

    Turno findTopByOrderByIdDesc();

    Turno findFirstByCategoriaIdAndPreferenteAndEstadoOrderByFechaCreacionAsc(Long categoriaId, boolean preferente, String estado);

    Turno findTopByCategoriaIdAndArchivadoFalseAndNumeroStartingWithOrderByIdDesc(Long categoriaId, String prefix);

    Turno findFirstByEstadoAndArchivadoFalseOrderByFechaActualizacionDesc(String estado);

    List<Turno> findTop5ByEstadoAndArchivadoFalseOrderByFechaActualizacionDesc(String estado);

    // Encuentra el turno actual atendido por un puesto (que no ha sido finalizado)
    Turno findFirstByPuestoAndEstadoAndFechaFinAtencionIsNullOrderByFechaLlamadaDesc(Integer puesto, String estado);

    // Encuentra el último turno atendido por un puesto (incluso si ya fue "finalizado" manualmente sin calificar)
    Turno findFirstByPuestoAndEstadoOrderByFechaLlamadaDesc(Integer puesto, String estado);

    // Métodos para métricas
    List<Turno> findByAtendidoPorIdAndEstadoAndFechaCreacionBetween(Long usuarioId, String estado, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    List<Turno> findByEstadoAndFechaCreacionBetween(String estado, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    List<Turno> findByFechaCreacionBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Turno t WHERE t.categoria.id = :categoriaId AND t.preferente = :preferente AND t.estado = 'abierto' AND (t.puesto IS NULL OR t.puesto = :puesto) ORDER BY t.fechaCreacion ASC")
    List<Turno> buscarSiguienteTurno(@org.springframework.data.repository.query.Param("categoriaId") Long categoriaId, @org.springframework.data.repository.query.Param("preferente") boolean preferente, @org.springframework.data.repository.query.Param("puesto") Integer puesto);
}
