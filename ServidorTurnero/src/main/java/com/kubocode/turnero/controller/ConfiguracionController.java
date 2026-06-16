package com.kubocode.turnero.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubocode.turnero.dto.MensajesPantallaDTO;

@RestController
@RequestMapping("/api/config/mensajes")
public class ConfiguracionController {

    @Value("${carrusel.ruta:carrusel}")
    private String carruselDir;

    private Path getDirectorio() throws IOException {
        Path dir = Paths.get(carruselDir).toAbsolutePath();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    @GetMapping
    public MensajesPantallaDTO getMensajes() {
        try {
            Path file = getDirectorio().resolve("mensajes.json");
            if (Files.exists(file)) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(file.toFile(), MensajesPantallaDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Valores por defecto si el archivo no existe o falla
        MensajesPantallaDTO def = new MensajesPantallaDTO();
        def.setMensajeHeader("¡Bienvenidos! Manténganse atentos a su turno");
        def.setMensajeFooter("Sistema desarrollado por KuboCode - Mantenga la distancia y espere su turno");
        return def;
    }

    @PostMapping
    public ResponseEntity<String> saveMensajes(@RequestBody MensajesPantallaDTO dto) {
        try {
            Path file = getDirectorio().resolve("mensajes.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file.toFile(), dto);
            return ResponseEntity.ok("Mensajes actualizados correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al guardar mensajes: " + e.getMessage());
        }
    }
}
