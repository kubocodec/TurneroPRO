package com.turnero;

public class UsuarioLogeado {
    private static Usuario usuario;

    public static void guardarUsuario(Usuario u) {
        usuario = u;
    }

    public static Usuario obtenerUsuario() {
        return usuario;
    }

    public static void cerrarSesion() {
        usuario = null;
    }
}
