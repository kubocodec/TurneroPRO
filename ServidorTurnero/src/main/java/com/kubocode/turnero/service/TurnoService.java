package com.kubocode.turnero.service;

import com.kubocode.turnero.model.Categoria;
import com.kubocode.turnero.model.Turno;
import com.kubocode.turnero.model.Usuario;
import com.kubocode.turnero.repository.CategoriaRepository;
import com.kubocode.turnero.repository.TurnoRepository;
import com.kubocode.turnero.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TurnoService implements ITurnoService{

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public Turno guardarTurno(Turno turno) {
        turno.setEstado("abierto");
        turno.setFechaCreacion(LocalDateTime.now());

        // Cargar categorÃ­a desde la BD usando su ID
        Long categoriaId = turno.getCategoria().getId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("CategorÃ­a no encontrada con ID: " + categoriaId));

        // Obtener prefijo desde nombre
        String prefijo = categoria.getNombre().substring(0, 1).toUpperCase();

        // Buscar Ãºltimo turno en esa categorÃ­a que tenga el prefijo original
        Turno ultimo = turnoRepository.findTopByCategoriaIdAndArchivadoFalseAndNumeroStartingWithOrderByIdDesc(categoriaId, prefijo);

        int siguienteNumero = 1;
        if (ultimo != null && ultimo.getNumero() != null) {
            try {
                String ultimoNumeroStr = ultimo.getNumero().substring(1); // ej: "P005" â†’ "005"
                siguienteNumero = Integer.parseInt(ultimoNumeroStr) + 1;
            } catch (NumberFormatException e) {
                siguienteNumero = 1;
            }
        }

        String numeroFormateado = String.format("%s%03d", prefijo, siguienteNumero);
        turno.setNumero(numeroFormateado);

        // Establecer la categorÃ­a completa cargada
        turno.setCategoria(categoria);

        return turnoRepository.save(turno);
    }



    @Override
    public List<Turno> obtenerTurnosAbiertosPorPreferencia(boolean preferente) {
        return turnoRepository.findByEstado("abierto")
                .stream()
                .filter(t -> Objects.equals(t.getPreferente(), preferente))
                .collect(Collectors.toList());
    }

    @Override
    public Turno avanzarSiguienteTurno(Long categoriaId, boolean preferente) {
        List<Turno> turnos = turnoRepository.buscarSiguienteTurno(categoriaId, preferente, null); // For avanzarSiguiente without puesto

        if (!turnos.isEmpty()) {
            Turno siguiente = turnos.get(0);
            siguiente.setEstado("en_atencion");
            return turnoRepository.save(siguiente);
        }
        return null;
    }

    @Override
    public Map<String, Long> contarTurnosPorCategoria() {
        List<Turno> turnos = turnoRepository.findByEstado("abierto");
        return turnos.stream()
                .collect(Collectors.groupingBy(t -> t.getCategoria().getNombre(), Collectors.counting()));
    }

    @Override
    public List<Turno> obtenerUltimosTurnosAtendidos(int limite) {
        return turnoRepository.findTop5ByEstadoAndArchivadoFalseOrderByFechaCreacionDesc("atendido");
    }

    @Override
    public Turno obtenerUltimoTurno() {
        return turnoRepository.findTopByOrderByIdDesc();
    }

    @Override
    public Turno cerrarTurno(Long categoriaId, boolean preferente, Long usuarioId, Integer puesto) {
        List<Turno> turnos = turnoRepository.buscarSiguienteTurno(categoriaId, preferente, puesto);
        Turno turno = turnos.isEmpty() ? null : turnos.get(0);

        if (turno == null) {
            throw new RuntimeException("No hay turnos abiertos disponibles.");
        }

        turno.setEstado("atendido");
        turno.setPuesto(puesto);
        turno.setFechaLlamada(LocalDateTime.now());
        turno.setFechaActualizacion(LocalDateTime.now());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        turno.setAtendidoPor(usuario);

        return turnoRepository.save(turno);
    }

    @Override
    public Turno obtenerUltimoTurnoProcesado() {
        return turnoRepository.findFirstByEstadoAndArchivadoFalseOrderByFechaActualizacionDesc("atendido");
    }

    @Override
    public List<Turno> obtenerUltimosTurnosAtendidos() {
        return turnoRepository.findTop5ByEstadoAndArchivadoFalseOrderByFechaActualizacionDesc("atendido");
    }

    @Override
    public void reiniciarTurnos() {
        List<Turno> todos = turnoRepository.findAll();
        for (Turno t : todos) {
            t.setArchivado(true);
            if ("abierto".equals(t.getEstado())) {
                t.setEstado("cancelado");
            }
        }
        turnoRepository.saveAll(todos);
    }

    @Override
    public Turno rellamarTurno(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con ID: " + id));

        Integer currentLlamadas = turno.getCantidadLlamadas();
        turno.setCantidadLlamadas(currentLlamadas != null ? currentLlamadas + 1 : 2);
        turno.setFechaActualizacion(LocalDateTime.now());
        
        return turnoRepository.save(turno);
    }

    @Override
    public Turno finalizarAtencion(Long turnoId, String calificacion, String observaciones) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
                
        // Prevenir que el frontend sobreescriba una calificación ya dada por la botonera física
        if (turno.getCalificacion() != null && !turno.getCalificacion().equals("NO CALIFICADO")) {
            if ("NO CALIFICADO".equals(calificacion)) {
                // Si ya fue calificado (ej. por hardware) y el frontend manda un cierre automático sin calificar, lo ignoramos.
                return turno;
            }
        }
                
        // Si no tenía fecha de fin, se la ponemos
        if (turno.getFechaFinAtencion() == null) {
            turno.setFechaFinAtencion(LocalDateTime.now());
        }
        
        turno.setCalificacion(calificacion);
        turno.setObservaciones(observaciones);
        turno.setFechaActualizacion(LocalDateTime.now());
        return turnoRepository.save(turno);
    }

    @Override
    public Turno finalizarAtencionPorPuesto(Integer puesto, String calificacion) {
        Turno turno = turnoRepository.findFirstByPuestoAndEstadoOrderByFechaLlamadaDesc(puesto, "atendido");
        if (turno == null) {
            System.out.println("No se encontró ningún turno activo o reciente para el puesto " + puesto);
            return null;
        }
        return finalizarAtencion(turno.getId(), calificacion, "Calificado a través de botonera física");
    }

    @Override
    public com.kubocode.turnero.dto.MetricasUsuarioDTO obtenerMetricasUsuario(Long usuarioId, LocalDateTime start, LocalDateTime end) {
        List<Turno> turnos = turnoRepository.findByAtendidoPorIdAndEstadoAndFechaCreacionBetween(usuarioId, "atendido", start, end);
        
        long totalAtendidos = 0;
        long tiempoEsperaTotal = 0;
        long tiempoAtencionTotal = 0;
        long rellamadasTotales = 0;
        Map<String, Long> distCalificaciones = new java.util.HashMap<>();
        int countConCalificacion = 0;
        List<com.kubocode.turnero.dto.TurnoDetalleDTO> detalles = new java.util.ArrayList<>();

        for (Turno t : turnos) {
            if (t.getFechaLlamada() != null) {
                totalAtendidos++;
                long espera = java.time.Duration.between(t.getFechaCreacion(), t.getFechaLlamada()).getSeconds();
                tiempoEsperaTotal += espera;
                
                long atencion = 0;
                if (t.getFechaFinAtencion() != null) {
                    atencion = java.time.Duration.between(t.getFechaLlamada(), t.getFechaFinAtencion()).getSeconds();
                    tiempoAtencionTotal += atencion;
                }
                
                if (t.getCalificacion() != null) {
                    countConCalificacion++;
                    distCalificaciones.put(t.getCalificacion(), distCalificaciones.getOrDefault(t.getCalificacion(), 0L) + 1);
                }
                
                if (t.getCantidadLlamadas() != null && t.getCantidadLlamadas() > 1) {
                    rellamadasTotales += (t.getCantidadLlamadas() - 1);
                }

                detalles.add(new com.kubocode.turnero.dto.TurnoDetalleDTO(
                        t.getNumero(),
                        t.getCategoria() != null ? t.getCategoria().getNombre() : "N/A",
                        t.getFechaLlamada().toString(),
                        t.getFechaFinAtencion() != null ? t.getFechaFinAtencion().toString() : "",
                        espera,
                        atencion,
                        t.getCalificacion() != null ? t.getCalificacion() : "N/A",
                        t.getObservaciones() != null ? t.getObservaciones() : ""
                ));
            }
        }

        com.kubocode.turnero.dto.MetricasUsuarioDTO dto = new com.kubocode.turnero.dto.MetricasUsuarioDTO();
        dto.setTotalTurnosAtendidos(totalAtendidos);
        dto.setTiempoPromedioEsperaSegundos(totalAtendidos > 0 ? (double) tiempoEsperaTotal / totalAtendidos : 0);
        dto.setTiempoPromedioAtencionSegundos(totalAtendidos > 0 ? (double) tiempoAtencionTotal / totalAtendidos : 0);
        dto.setDistribucionCalificaciones(distCalificaciones);
        dto.setTotalRellamadas(rellamadasTotales);
        dto.setDetalleTurnos(detalles);
        
        // Calcular calificacion promedio real
        long sumaPonderada = 0;
        long totalVotos = 0;
        for (Map.Entry<String, Long> e : distCalificaciones.entrySet()) {
            long count = e.getValue();
            if ("EXCELENTE".equals(e.getKey())) { sumaPonderada += 4 * count; totalVotos += count; }
            else if ("BUENO".equals(e.getKey())) { sumaPonderada += 3 * count; totalVotos += count; }
            else if ("REGULAR".equals(e.getKey())) { sumaPonderada += 2 * count; totalVotos += count; }
            else if ("MALO".equals(e.getKey())) { sumaPonderada += 1 * count; totalVotos += count; }
        }

        if (totalVotos > 0) {
            double promedioNum = (double) sumaPonderada / totalVotos;
            double porcentaje = (promedioNum / 4.0) * 100.0;
            String nivel = "Malo";
            if (promedioNum >= 3.5) nivel = "Excelente";
            else if (promedioNum >= 2.5) nivel = "Bueno";
            else if (promedioNum >= 1.5) nivel = "Regular";
            dto.setCalificacionPromedio(String.format("%.1f%% (%s)", porcentaje, nivel));
        } else {
            dto.setCalificacionPromedio("N/A");
        }
        
        return dto;
    }

    @Override
    public com.kubocode.turnero.dto.MetricasGeneralesDTO obtenerMetricasGenerales(LocalDateTime start, LocalDateTime end) {
        List<Turno> todos = turnoRepository.findByFechaCreacionBetween(start, end);
        long pendientes = todos.stream().filter(t -> "abierto".equals(t.getEstado())).count();
        List<Turno> atendidos = todos.stream().filter(t -> "atendido".equals(t.getEstado()) && t.getFechaLlamada() != null).collect(Collectors.toList());
        
        long tiempoEsperaTotal = 0;
        long tiempoAtencionTotal = 0;
        java.util.Map<String, Long> globalCalifs = new java.util.HashMap<>();
        
        for (Turno t : atendidos) {
            tiempoEsperaTotal += java.time.Duration.between(t.getFechaCreacion(), t.getFechaLlamada()).getSeconds();
            if (t.getFechaFinAtencion() != null) {
                tiempoAtencionTotal += java.time.Duration.between(t.getFechaLlamada(), t.getFechaFinAtencion()).getSeconds();
            }
            if (t.getCalificacion() != null && !t.getCalificacion().equals("NO CALIFICADO") && !t.getCalificacion().equals("TRANSFERIDO")) {
                globalCalifs.put(t.getCalificacion(), globalCalifs.getOrDefault(t.getCalificacion(), 0L) + 1);
            }
        }
        
        com.kubocode.turnero.dto.MetricasGeneralesDTO dto = new com.kubocode.turnero.dto.MetricasGeneralesDTO();
        dto.setTotalTurnosDelDia(todos.size());
        dto.setTurnosPendientes(pendientes);
        dto.setTurnosAtendidos(atendidos.size());
        dto.setTiempoPromedioEsperaGeneral(atendidos.isEmpty() ? 0 : (double) tiempoEsperaTotal / atendidos.size());
        dto.setTiempoPromedioAtencionGeneral(atendidos.isEmpty() ? 0 : (double) tiempoAtencionTotal / atendidos.size());
        dto.setDistribucionCalificaciones(globalCalifs);
        
        Map<Long, List<Turno>> porUsuario = atendidos.stream()
                .filter(t -> t.getAtendidoPor() != null)
                .collect(Collectors.groupingBy(t -> t.getAtendidoPor().getId()));
                
        List<com.kubocode.turnero.dto.UsuarioMetricaDTO> usuariosList = new java.util.ArrayList<>();
        for (Map.Entry<Long, List<Turno>> entry : porUsuario.entrySet()) {
            List<Turno> tUsr = entry.getValue();
            long uEsperaTotal = 0;
            long uAtencionTotal = 0;
            Map<String, Long> uCalifs = new java.util.HashMap<>();
            for(Turno t : tUsr) {
                uEsperaTotal += java.time.Duration.between(t.getFechaCreacion(), t.getFechaLlamada()).getSeconds();
                if(t.getFechaFinAtencion() != null) {
                    uAtencionTotal += java.time.Duration.between(t.getFechaLlamada(), t.getFechaFinAtencion()).getSeconds();
                }
                if(t.getCalificacion() != null) {
                    uCalifs.put(t.getCalificacion(), uCalifs.getOrDefault(t.getCalificacion(), 0L) + 1);
                }
            }
            
            String mejor = "N/A";
            long sumaPonderada = 0;
            long totalVotos = 0;
            for (Map.Entry<String, Long> e : uCalifs.entrySet()) {
                long count = e.getValue();
                if ("EXCELENTE".equals(e.getKey())) { sumaPonderada += 4 * count; totalVotos += count; }
                else if ("BUENO".equals(e.getKey())) { sumaPonderada += 3 * count; totalVotos += count; }
                else if ("REGULAR".equals(e.getKey())) { sumaPonderada += 2 * count; totalVotos += count; }
                else if ("MALO".equals(e.getKey())) { sumaPonderada += 1 * count; totalVotos += count; }
            }
            if (totalVotos > 0) {
                double promedioNum = (double) sumaPonderada / totalVotos;
                double porcentaje = (promedioNum / 4.0) * 100.0;
                String nivel = "Malo";
                if (promedioNum >= 3.5) nivel = "Excelente";
                else if (promedioNum >= 2.5) nivel = "Bueno";
                else if (promedioNum >= 1.5) nivel = "Regular";
                mejor = String.format("%.1f%% (%s)", porcentaje, nivel);
            }
            
            String uName = tUsr.get(0).getAtendidoPor().getNombre();
            double avgE = tUsr.isEmpty() ? 0 : (double) uEsperaTotal / tUsr.size();
            double avgA = tUsr.isEmpty() ? 0 : (double) uAtencionTotal / tUsr.size();
            
            usuariosList.add(new com.kubocode.turnero.dto.UsuarioMetricaDTO(entry.getKey(), uName, tUsr.size(), avgE, avgA, mejor));
        }
        
        dto.setMetricasPorUsuario(usuariosList);
        return dto;
    }
    @Override
    public Turno transferirTurno(Long turnoId, Long nuevaCategoriaId, Integer puesto) {
        Turno turnoActual = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        Categoria nuevaCategoria = categoriaRepository.findById(nuevaCategoriaId)
                .orElseThrow(() -> new RuntimeException("CategorÃ­a no encontrada con ID: " + nuevaCategoriaId));

        // Finalizar el turno actual con calificación "TRANSFERIDO"
        turnoActual.setEstado("atendido");
        turnoActual.setFechaFinAtencion(LocalDateTime.now());
        turnoActual.setCalificacion("TRANSFERIDO");
        turnoActual.setObservaciones("Transferido a la categoría: " + nuevaCategoria.getNombre());
        turnoActual.setFechaActualizacion(LocalDateTime.now());
        turnoRepository.save(turnoActual);

        // Crear un nuevo turno clonado en la nueva categorÃ­a
        Turno nuevoTurno = new Turno();
        nuevoTurno.setNumero(turnoActual.getNumero()); // Mantiene el mismo nÃºmero
        nuevoTurno.setCategoria(nuevaCategoria);
        nuevoTurno.setEstado("abierto");
        nuevoTurno.setPreferente(turnoActual.getPreferente());
        nuevoTurno.setFechaCreacion(turnoActual.getFechaCreacion());
        nuevoTurno.setFechaActualizacion(LocalDateTime.now());
        nuevoTurno.setArchivado(false);
        nuevoTurno.setTransferido(true);
        if (puesto != null) {
            nuevoTurno.setPuesto(puesto);
        }

        return turnoRepository.save(nuevoTurno);
    }
}
