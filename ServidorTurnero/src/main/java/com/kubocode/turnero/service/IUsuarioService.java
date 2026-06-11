package com.kubocode.turnero.service;

import com.kubocode.turnero.model.Usuario;
import java.util.List;

public interface IUsuarioService {
    Usuario verificarUsuario(String username, String password);
    List<Usuario> listarUsuarios();
    Usuario guardarUsuario(Usuario u);
    Usuario actualizarUsuario(Long id, Usuario u);
    void eliminarUsuario(Long id);
}
