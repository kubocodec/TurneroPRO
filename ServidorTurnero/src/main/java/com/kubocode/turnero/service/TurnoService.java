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

        // Cargar categoría desde la BD usando su ID
        Long categoriaId = turno.getCategoria().getId();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));

        // Obtener prefijo desde nombre
        String prefijo = categoria.getNombre().substring(0, 1).toUpperCase();

        // Buscar último turno en esa categoría
        Turno ultimo = turnoRepository.findTopByCategoriaIdOrderByIdDesc(categoriaId);

        int siguienteNumero = 1;
        if (ultimo != null && ultimo.getNumero() != null) {
            try {
                String ultimoNumeroStr = ultimo.getNumero().substring(1); // ej: "P005" → "005"
                siguienteNumero = Integer.parseInt(ultimoNumeroStr) + 1;
            } catch (NumberFormatException e) {
                siguienteNumero = 1;
            }
        }

        String numeroFormateado = String.format("%s%03d", prefijo, siguienteNumero);
        turno.setNumero(numeroFormateado);

        // Establecer la categoría completa cargada
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
        List<Turno> turnos = turnoRepository.findByCategoriaIdAndPreferenteAndEstadoOrderByFechaCreacionAsc(
                categoriaId, preferente, "abierto"
        );

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
        return turnoRepository.findTop5ByEstadoOrderByFechaCreacionDesc("atendido");
    }

    @Override
    public Turno obtenerUltimoTurno() {
        return turnoRepository.findTopByOrderByIdDesc();
    }

    @Override
    public Turno cerrarTurno(Long categoriaId, boolean preferente, Long usuarioId, Integer puesto) {
        Turno turno = turnoRepository.findFirstByCategoriaIdAndPreferenteAndEstadoOrderByFechaCreacionAsc(
                categoriaId, preferente, "abierto"
        );

        if (turno == null) {
            throw new RuntimeException("No hay turnos abiertos disponibles.");
        }

        turno.setEstado("atendido");
        turno.setPuesto(puesto);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        turno.setAtendidoPor(usuario);

        return turnoRepository.save(turno);
    }

    @Override
    public Turno obtenerUltimoTurnoProcesado() {
        return turnoRepository.findFirstByEstadoOrderByFechaActualizacionDesc("atendido");
    }

    @Override
    public List<Turno> obtenerUltimosTurnosAtendidos() {
        return turnoRepository.findTop5ByEstadoOrderByFechaActualizacionDesc("atendido");
    }

    @Override
    public void reiniciarTurnos() {
        turnoRepository.deleteAll();
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

}
