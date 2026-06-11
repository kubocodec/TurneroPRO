package com.turnero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VistaUsuarios {

    private TableView<Usuario> tabla = new TableView<>();
    private TextField txtNombre = new TextField();
    private TextField txtUsername = new TextField();
    private PasswordField txtPassword = new PasswordField();
    private ComboBox<String> comboRol = new ComboBox<>();
    private Usuario usuarioSeleccionado = null;
    private Label lblMensaje = new Label();

    public VBox construir() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titulo = new Label("GESTIÓN DE USUARIOS");
        titulo.setGraphic(org.kordamp.ikonli.javafx.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.USERS, 22, javafx.scene.paint.Color.web("#2c3e50")));
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        // Tabla
        TableColumn<Usuario, String> colId      = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setPrefWidth(60);
        TableColumn<Usuario, String> colNombre  = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        TableColumn<Usuario, String> colUser    = new TableColumn<>("Username");
        colUser.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUsername()));
        TableColumn<Usuario, String> colRol     = new TableColumn<>("Rol");
        colRol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRol()));
        colRol.setPrefWidth(100);

        tabla.getColumns().addAll(colId, colNombre, colUser, colRol);
        tabla.setPrefHeight(220);
        tabla.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tabla, Priority.ALWAYS);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                usuarioSeleccionado = nuevo;
                txtNombre.setText(nuevo.getNombre());
                txtUsername.setText(nuevo.getUsername());
                txtPassword.clear();
                comboRol.setValue(nuevo.getRol());
            }
        });

        // Formulario
        VBox formulario = new VBox(10);
        formulario.setPadding(new Insets(15));
        formulario.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,3);");
        formulario.setMaxWidth(500);

        comboRol.getItems().addAll("ADMIN", "USER");
        comboRol.setValue("USER");

        txtNombre.setPromptText("Nombre completo");
        txtUsername.setPromptText("Username");
        txtPassword.setPromptText("Contraseña (dejar vacío para no cambiar)");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.add(new Label("Nombre:"), 0, 0);   grid.add(txtNombre, 1, 0);
        grid.add(new Label("Username:"), 0, 1);  grid.add(txtUsername, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2); grid.add(txtPassword, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);       grid.add(comboRol, 1, 3);

        Button btnGuardar  = crearBoton("Guardar", "#27ae60", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SAVE);
        Button btnEditar   = crearBoton("Actualizar", "#2980b9", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PENCIL_ALT);
        Button btnEliminar = crearBoton("Eliminar", "#c0392b", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TRASH);
        Button btnLimpiar  = crearBoton("Limpiar", "#7f8c8d", org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES);

        btnGuardar.setOnAction(e -> guardarUsuario());
        btnEditar.setOnAction(e -> actualizarUsuario());
        btnEliminar.setOnAction(e -> eliminarUsuario());
        btnLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, btnGuardar, btnEditar, btnEliminar, btnLimpiar);

        lblMensaje.setFont(Font.font("Arial", 13));
        formulario.getChildren().addAll(grid, botones, lblMensaje);

        root.getChildren().addAll(titulo, tabla, formulario);
        cargarUsuarios();
        return root;
    }

    private Button crearBoton(String texto, String color, org.kordamp.ikonli.Ikon icon) {
        Button b = new Button(texto);
        b.setGraphic(org.kordamp.ikonli.javafx.FontIcon.of(icon, 14, Color.WHITE));
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13px; -fx-cursor: hand;");
        b.setPrefHeight(35);
        return b;
    }

    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/usuarios/lista");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                List<Usuario> lista = new ObjectMapper().readValue(conn.getInputStream(), new TypeReference<List<Usuario>>() {});
                Platform.runLater(() -> tabla.getItems().setAll(lista));
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar usuarios", false));
            }
        }).start();
    }

    private void guardarUsuario() {
        if (txtNombre.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() || txtPassword.getText().trim().isEmpty()) {
            mostrarMensaje("Completa nombre, username y contraseña", false); return;
        }
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/usuarios/guardar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                Map<String, String> body = buildBody();
                new ObjectMapper().writeValue(conn.getOutputStream(), body);
                int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) { mostrarMensaje("✅ Usuario guardado", true); cargarUsuarios(); limpiar(); }
                    else mostrarMensaje("Error al guardar", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
            }
        }).start();
    }

    private void actualizarUsuario() {
        if (usuarioSeleccionado == null) { mostrarMensaje("Selecciona un usuario", false); return; }
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/usuarios/editar/" + usuarioSeleccionado.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                new ObjectMapper().writeValue(conn.getOutputStream(), buildBody());
                int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) { mostrarMensaje("✅ Usuario actualizado", true); cargarUsuarios(); limpiar(); }
                    else mostrarMensaje("Error al actualizar", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
            }
        }).start();
    }

    private void eliminarUsuario() {
        if (usuarioSeleccionado == null) { mostrarMensaje("Selecciona un usuario", false); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar al usuario \"" + usuarioSeleccionado.getUsername() + "\"?", ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar eliminación");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/usuarios/eliminar/" + usuarioSeleccionado.getId());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE");
                        int code = conn.getResponseCode();
                        Platform.runLater(() -> {
                            if (code == 200) { mostrarMensaje("✅ Eliminado", true); cargarUsuarios(); limpiar(); }
                            else mostrarMensaje("Error al eliminar", false);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
                    }
                }).start();
            }
        });
    }

    private Map<String, String> buildBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("nombre", txtNombre.getText().trim());
        m.put("username", txtUsername.getText().trim());
        m.put("password", txtPassword.getText().trim());
        m.put("rol", comboRol.getValue());
        return m;
    }

    private void limpiar() {
        txtNombre.clear(); txtUsername.clear(); txtPassword.clear();
        comboRol.setValue("USER"); usuarioSeleccionado = null;
        tabla.getSelectionModel().clearSelection(); lblMensaje.setText("");
    }

    private void mostrarMensaje(String msg, boolean ok) {
        lblMensaje.setText(msg);
        lblMensaje.setTextFill(ok ? Color.web("#27ae60") : Color.web("#c0392b"));
    }
}
