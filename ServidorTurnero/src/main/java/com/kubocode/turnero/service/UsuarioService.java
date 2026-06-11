package com.kubocode.turnero.service;

import com.kubocode.turnero.model.Usuario;
import com.kubocode.turnero.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Usuario verificarUsuario(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(password)) {
                return usuario;
            }
        }
        return null;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario guardarUsuario(Usuario u) {
        return usuarioRepository.save(u);
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario u) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        existente.setNombre(u.getNombre());
        existente.setUsername(u.getUsername());
        existente.setRol(u.getRol());
        if (u.getPassword() != null && !u.getPassword().trim().isEmpty()) {
            existente.setPassword(u.getPassword());
        }
        return usuarioRepository.save(existente);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
