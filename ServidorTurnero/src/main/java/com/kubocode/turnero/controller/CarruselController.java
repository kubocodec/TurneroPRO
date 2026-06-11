package com.kubocode.turnero.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/carrusel")
public class CarruselController {

    @Value("${carrusel.ruta:carrusel}")
    private String carruselDir;

    private Path getDirectorio() throws IOException {
        Path dir = Paths.get(carruselDir).toAbsolutePath();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    @GetMapping("/lista")
    public List<String> listarImagenes() {
        try {
            Path dir = getDirectorio();
            File[] archivos = dir.toFile().listFiles(
                    f -> f.isFile() && (f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.getName().endsWith(".jpeg"))
            );
            if (archivos == null) return Collections.emptyList();
            
            List<String> todasLasImagenes = Arrays.stream(archivos).map(File::getName).collect(Collectors.toList());
            
            // Intentar leer orden.json
            Path ordenPath = dir.resolve("orden.json");
            if (Files.exists(ordenPath)) {
                ObjectMapper mapper = new ObjectMapper();
                List<String> ordenadas = mapper.readValue(ordenPath.toFile(), new TypeReference<List<String>>() {});
                
                // Filtrar las que existen realmente
                List<String> resultado = ordenadas.stream()
                        .filter(todasLasImagenes::contains)
                        .collect(Collectors.toList());
                        
                // Agregar las nuevas que no están en el orden
                for (String img : todasLasImagenes) {
                    if (!resultado.contains(img)) {
                        resultado.add(img);
                    }
                }
                return resultado;
            }
            
            return todasLasImagenes;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @PostMapping("/orden")
    public ResponseEntity<String> guardarOrden(@RequestBody List<String> orden) {
        try {
            Path dir = getDirectorio();
            Path ordenPath = dir.resolve("orden.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(ordenPath.toFile(), orden);
            return ResponseEntity.ok("Orden guardado correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al guardar orden: " + e.getMessage());
        }
    }

    @PostMapping("/subir")
    public ResponseEntity<String> subirImagen(@RequestParam("archivo") MultipartFile archivo) {
        try {
            Path dir = getDirectorio();
            String nombre = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path destino = dir.resolve(nombre);
            Files.write(destino, archivo.getBytes());
            return ResponseEntity.ok(nombre);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir imagen: " + e.getMessage());
        }
    }

    @GetMapping("/imagen/{nombre}")
    public ResponseEntity<Resource> obtenerImagen(@PathVariable String nombre) {
        try {
            Path archivo = getDirectorio().resolve(nombre);
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(archivo);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/eliminar/{nombre}")
    public ResponseEntity<String> eliminarImagen(@PathVariable String nombre) {
        try {
            Path archivo = getDirectorio().resolve(nombre);
            Files.deleteIfExists(archivo);
            return ResponseEntity.ok("Imagen eliminada correctamente");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al eliminar imagen: " + e.getMessage());
        }
    }
}
