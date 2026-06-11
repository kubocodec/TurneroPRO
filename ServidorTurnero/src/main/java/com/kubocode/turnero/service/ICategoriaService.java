package com.kubocode.turnero.service;

import com.kubocode.turnero.model.Categoria;

import java.util.List;

public interface ICategoriaService {
    List<Categoria> listarCategorias();
    Categoria getCategoria(Long id);
    void guardarCategoria(Categoria categoria);
    void actualizarCategoria(Long id,Categoria categoria);
    void eliminarCategoria(Long id);


}
