package com.kubocode.turnero.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/config/logo")
public class LogoController {

    @Value("${carrusel.ruta:carrusel}")
    private String carruselDir;

    private Path getDirectorio() throws IOException {
        Path dir = Paths.get(carruselDir).toAbsolutePath();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    @PostMapping
    public ResponseEntity<String> subirLogo(@RequestParam("archivo") MultipartFile archivo) {
        try {
            Path dir = getDirectorio();
            Path destino = dir.resolve("logo.png");
            Files.write(destino, archivo.getBytes());
            return ResponseEntity.ok("logo.png");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir el logo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Resource> obtenerLogo() {
        try {
            Path archivo = getDirectorio().resolve("logo.png");
            if (!Files.exists(archivo)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(archivo.toUri());
            String contentType = Files.probeContentType(archivo);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/png"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"logo.png\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<String> eliminarLogo() {
        try {
            Path archivo = getDirectorio().resolve("logo.png");
            if (Files.deleteIfExists(archivo)) {
                return ResponseEntity.ok("Logo eliminado correctamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al eliminar el logo: " + e.getMessage());
        }
    }
}
