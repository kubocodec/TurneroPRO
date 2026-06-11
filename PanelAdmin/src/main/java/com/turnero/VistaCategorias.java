package com.turnero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class VistaCategorias {

    private TableView<Categoria> tabla = new TableView<>();
    private TextField txtNombre = new TextField();
    private Categoria categoriaSeleccionada = null;
    private Label lblMensaje = new Label();

    public VBox construir() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titulo = new Label("GESTIÓN DE CATEGORÍAS");
        titulo.setGraphic(org.kordamp.ikonli.javafx.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FOLDER_OPEN, 22, Color.web("#2c3e50")));
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web("#2c3e50"));

        // Tabla
        TableColumn<Categoria, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setPrefWidth(80);

        TableColumn<Categoria, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(300);

        tabla.getColumns().addAll(colId, colNombre);
        tabla.setPrefHeight(250);
        tabla.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tabla, Priority.ALWAYS);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            if (nueva != null) {
                categoriaSeleccionada = nueva;
                txtNombre.setText(nueva.getNombre());
            }
        });

        // Formulario
        VBox formulario = new VBox(10);
        formulario.setPadding(new Insets(15));
        formulario.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,3);");
        formulario.setMaxWidth(400);

        Label lblForm = new Label("Nombre de categoría:");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        txtNombre.setPromptText("Ej: General, Preferencial...");
        txtNombre.setPrefHeight(35);

        Button btnGuardar = crearBoton("Guardar", "#27ae60", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SAVE);
        Button btnEditar  = crearBoton("Actualizar", "#2980b9", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PENCIL_ALT);
        Button btnEliminar = crearBoton("Eliminar", "#c0392b", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH);
        Button btnLimpiar = crearBoton("Limpiar", "#7f8c8d", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES);

        btnGuardar.setOnAction(e -> guardarCategoria());
        btnEditar.setOnAction(e -> actualizarCategoria());
        btnEliminar.setOnAction(e -> eliminarCategoria());
        btnLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, btnGuardar, btnEditar, btnEliminar, btnLimpiar);

        lblMensaje.setFont(Font.font("Arial", 13));

        formulario.getChildren().addAll(lblForm, txtNombre, botones, lblMensaje);
        root.getChildren().addAll(titulo, tabla, formulario);

        cargarCategorias();
        return root;
    }

    private Button crearBoton(String texto, String color, org.kordamp.ikonli.Ikon icon) {
        Button b = new Button(texto);
        b.setGraphic(org.kordamp.ikonli.javafx.FontIcon.of(icon, 14, Color.WHITE));
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13px; -fx-cursor: hand;");
        b.setPrefHeight(35);
        return b;
    }

    private void cargarCategorias() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/lista");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                List<Categoria> lista = new ObjectMapper().readValue(conn.getInputStream(), new TypeReference<List<Categoria>>() {});
                Platform.runLater(() -> {
                    tabla.getItems().setAll(lista);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar categorías", false));
            }
        }).start();
    }

    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { mostrarMensaje("Ingresa un nombre", false); return; }
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/guardar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                Map<String, String> body = new LinkedHashMap<>();
                body.put("nombre", nombre);
                new ObjectMapper().writeValue(conn.getOutputStream(), body);
                int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) { mostrarMensaje("✅ Categoría guardada", true); cargarCategorias(); limpiar(); }
                    else mostrarMensaje("Error al guardar", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
            }
        }).start();
    }

    private void actualizarCategoria() {
        if (categoriaSeleccionada == null) { mostrarMensaje("Selecciona una categoría", false); return; }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { mostrarMensaje("Ingresa un nombre", false); return; }
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/editar/" + categoriaSeleccionada.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                Map<String, String> body = new LinkedHashMap<>();
                body.put("nombre", nombre);
                new ObjectMapper().writeValue(conn.getOutputStream(), body);
                int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) { mostrarMensaje("✅ Categoría actualizada", true); cargarCategorias(); limpiar(); }
                    else mostrarMensaje("Error al actualizar", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
            }
        }).start();
    }

    private void eliminarCategoria() {
        if (categoriaSeleccionada == null) { mostrarMensaje("Selecciona una categoría", false); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar la categoría \"" + categoriaSeleccionada.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar eliminación");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/eliminar/" + categoriaSeleccionada.getId());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE");
                        int code = conn.getResponseCode();
                        Platform.runLater(() -> {
                            if (code == 200) { mostrarMensaje("✅ Eliminada", true); cargarCategorias(); limpiar(); }
                            else mostrarMensaje("Error al eliminar", false);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
                    }
                }).start();
            }
        });
    }

    private void limpiar() {
        txtNombre.clear();
        categoriaSeleccionada = null;
        tabla.getSelectionModel().clearSelection();
        lblMensaje.setText("");
    }

    private void mostrarMensaje(String msg, boolean ok) {
        lblMensaje.setText(msg);
        lblMensaje.setTextFill(ok ? Color.web("#27ae60") : Color.web("#c0392b"));
    }
}
