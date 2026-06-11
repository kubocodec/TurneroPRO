package com.kubocode.turnero.controller;

import com.kubocode.turnero.model.Usuario;
import com.kubocode.turnero.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUsuarioService usuarioService;

    // Clase interna para representar las credenciales
    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.verificarUsuario(request.username, request.password);
        if (usuario == null) {
            throw new RuntimeException("Credenciales incorrectas");
        }
        return usuario;
    }
}