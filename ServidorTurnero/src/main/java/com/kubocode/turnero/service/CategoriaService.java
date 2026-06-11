package com.kubocode.turnero.service;

import com.kubocode.turnero.model.Categoria;
import com.kubocode.turnero.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService implements ICategoriaService{
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria getCategoria(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public void guardarCategoria(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

    @Override
    public void actualizarCategoria(Long id, Categoria categoria) {
        Categoria categoria1=categoriaRepository.findById(id).orElse(null);
        categoria1.setId(id);
        categoria1.setNombre(categoria.getNombre());
        categoriaRepository.save(categoria1);
    }

    @Override
    public void eliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }
}
