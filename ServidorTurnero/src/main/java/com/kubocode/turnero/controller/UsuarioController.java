package com.kubocode.turnero.controller;

import com.kubocode.turnero.model.Usuario;
import com.kubocode.turnero.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("/lista")
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PostMapping("/guardar")
    public Usuario guardarUsuario(@RequestBody Usuario u) {
        return usuarioService.guardarUsuario(u);
    }

    @PutMapping("/editar/{id}")
    public Usuario actualizarUsuario(@PathVariable Long id, @RequestBody Usuario u) {
        return usuarioService.actualizarUsuario(id, u);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }
}
