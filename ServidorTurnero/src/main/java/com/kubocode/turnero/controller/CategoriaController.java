package com.kubocode.turnero.controller;

import com.kubocode.turnero.model.Categoria;
import com.kubocode.turnero.service.ICategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    @Autowired
    private ICategoriaService categoriaService;

    @GetMapping("/lista")
    public List<Categoria> listarCategorias() {
        return categoriaService.listarCategorias();
    }

    @GetMapping("/{id}")
    public Categoria getCategoria(@PathVariable Long id){
        return categoriaService.getCategoria(id);
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@RequestBody Categoria categoria){
        categoriaService.guardarCategoria(categoria);
        return"Categoria creada correctamente";
    }

    @PutMapping("/editar/{id}")
    public String actualizarCategoria(@PathVariable Long id,
                                      @RequestBody Categoria categoria){
        categoriaService.actualizarCategoria(id,categoria);
        return "Categoria actualizada correctamente";
    }

    @DeleteMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id){
        categoriaService.eliminarCategoria(id);
        return  "Categoria eliminada correctamente";
    }
}
