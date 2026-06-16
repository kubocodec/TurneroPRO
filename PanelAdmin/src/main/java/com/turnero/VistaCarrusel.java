package com.turnero;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import com.turnero.dto.MensajesPantallaDTO;

public class VistaCarrusel {

    private FlowPane galeria = new FlowPane(15, 15);
    private Label lblMensaje = new Label();
    private javafx.collections.ObservableList<String> listaNombres = javafx.collections.FXCollections.observableArrayList();

    public VBox construir() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label titulo = new Label("GESTIÓN DE IMÁGENES DEL CARRUSEL");
        titulo.setGraphic(FontIcon.of(FontAwesomeSolid.IMAGES, 22, Color.web("#2c3e50")));
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web("#2c3e50"));

        Button btnSubir = new Button("Subir imagen...");
        btnSubir.setGraphic(FontIcon.of(FontAwesomeSolid.UPLOAD, 14, Color.WHITE));
        btnSubir.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 14px; -fx-cursor: hand;");
        btnSubir.setPrefHeight(40);
        btnSubir.setOnAction(e -> subirImagen());

        Button btnGuardarOrden = new Button("Guardar orden");
        btnGuardarOrden.setGraphic(FontIcon.of(FontAwesomeSolid.SAVE, 14, Color.WHITE));
        btnGuardarOrden.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 14px; -fx-cursor: hand;");
        btnGuardarOrden.setPrefHeight(40);
        btnGuardarOrden.setOnAction(e -> guardarOrden());

        lblMensaje.setFont(Font.font("Arial", 13));

        HBox toolbar = new HBox(15, btnSubir, btnGuardarOrden, lblMensaje);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(galeria);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450);
        scroll.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        galeria.setPadding(new Insets(15));

        // --- SECCIÓN DE MENSAJES INFORMATIVOS ---
        Separator sep = new Separator();
        sep.setPadding(new Insets(15, 0, 15, 0));

        Label tituloMensajes = new Label("MENSAJES INFORMATIVOS DE LA PANTALLA");
        tituloMensajes.setGraphic(FontIcon.of(FontAwesomeSolid.COMMENT_ALT, 20, Color.web("#2c3e50")));
        tituloMensajes.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tituloMensajes.setTextFill(Color.web("#2c3e50"));

        GridPane gridMensajes = new GridPane();
        gridMensajes.setHgap(15);
        gridMensajes.setVgap(15);
        gridMensajes.setPadding(new Insets(20));
        gridMensajes.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);");

        Label lblHeader = new Label("Mensaje Superior (Header):");
        lblHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblHeader.setTextFill(Color.web("#2c3e50"));
        TextField txtHeader = new TextField();
        txtHeader.setPrefWidth(600);
        txtHeader.setPromptText("Ej. ¡Bienvenidos! Manténganse atentos a su turno");
        txtHeader.setStyle("-fx-background-radius: 6; -fx-padding: 8;");

        Label lblFooter = new Label("Mensaje Inferior (Footer/Marquesina):");
        lblFooter.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblFooter.setTextFill(Color.web("#2c3e50"));
        TextField txtFooter = new TextField();
        txtFooter.setPrefWidth(600);
        txtFooter.setPromptText("Ej. Sistema desarrollado por KuboCode - Mantenga la distancia y espere su turno");
        txtFooter.setStyle("-fx-background-radius: 6; -fx-padding: 8;");

        Button btnGuardarMensajes = new Button("Guardar Mensajes");
        btnGuardarMensajes.setGraphic(FontIcon.of(FontAwesomeSolid.SAVE, 13, Color.WHITE));
        btnGuardarMensajes.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnGuardarMensajes.setPrefHeight(35);

        Label lblStatusMensajes = new Label();
        lblStatusMensajes.setFont(Font.font("Arial", 13));

        HBox boxBotonMensajes = new HBox(15, btnGuardarMensajes, lblStatusMensajes);
        boxBotonMensajes.setAlignment(Pos.CENTER_LEFT);

        gridMensajes.add(lblHeader, 0, 0);
        gridMensajes.add(txtHeader, 1, 0);
        gridMensajes.add(lblFooter, 0, 1);
        gridMensajes.add(txtFooter, 1, 1);
        gridMensajes.add(boxBotonMensajes, 1, 2);

        // Cargar mensajes actuales del servidor al iniciar
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/config/mensajes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                if (conn.getResponseCode() == 200) {
                    MensajesPantallaDTO dto = new ObjectMapper().readValue(conn.getInputStream(), MensajesPantallaDTO.class);
                    Platform.runLater(() -> {
                        txtHeader.setText(dto.getMensajeHeader());
                        txtFooter.setText(dto.getMensajeFooter());
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblStatusMensajes.setText("⚠️ No se pudieron cargar los mensajes desde el servidor.");
                    lblStatusMensajes.setTextFill(Color.web("#d35400"));
                });
            }
        }).start();

        // Acción al guardar
        btnGuardarMensajes.setOnAction(ev -> {
            MensajesPantallaDTO dto = new MensajesPantallaDTO();
            dto.setMensajeHeader(txtHeader.getText().trim());
            dto.setMensajeFooter(txtFooter.getText().trim());

            lblStatusMensajes.setText("Guardando...");
            lblStatusMensajes.setTextFill(Color.web("#7f8c8d"));

            new Thread(() -> {
                try {
                    URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/config/mensajes");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    new ObjectMapper().writeValue(conn.getOutputStream(), dto);

                    int code = conn.getResponseCode();
                    Platform.runLater(() -> {
                        if (code == 200) {
                            lblStatusMensajes.setText("✅ Mensajes guardados correctamente");
                            lblStatusMensajes.setTextFill(Color.web("#27ae60"));
                        } else {
                            lblStatusMensajes.setText("❌ Error al guardar (código " + code + ")");
                            lblStatusMensajes.setTextFill(Color.web("#e74c3c"));
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        lblStatusMensajes.setText("❌ Error de conexión: " + ex.getMessage());
                        lblStatusMensajes.setTextFill(Color.web("#e74c3c"));
                    });
                }
            }).start();
        });

        // --- SECCIÓN DEL LOGO DE LA INSTITUCIÓN ---
        Separator sepLogo = new Separator();
        sepLogo.setPadding(new Insets(15, 0, 15, 0));

        Label tituloLogo = new Label("LOGO DE LA INSTITUCIÓN");
        tituloLogo.setGraphic(FontIcon.of(FontAwesomeSolid.BUILDING, 20, Color.web("#2c3e50")));
        tituloLogo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tituloLogo.setTextFill(Color.web("#2c3e50"));

        HBox logoContainer = new HBox(30);
        logoContainer.setPadding(new Insets(20));
        logoContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);");
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        ImageView currentLogoView = new ImageView();
        currentLogoView.setFitWidth(150);
        currentLogoView.setFitHeight(150);
        currentLogoView.setPreserveRatio(true);
        currentLogoView.setSmooth(true);
        
        StackPane logoPreview = new StackPane(currentLogoView);
        logoPreview.setPrefSize(180, 180);
        logoPreview.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1px;");

        VBox logoControls = new VBox(15);
        logoControls.setAlignment(Pos.CENTER_LEFT);

        Label lblLogoDesc = new Label("Sube el logo de la institución. Se mostrará en el turnero físico (kiosco) y se imprimirá en los tickets.\nFormato recomendado: PNG (fondo transparente o blanco).\nTamaño recomendado: 256x256 píxeles para una óptima impresión térmica.");
        lblLogoDesc.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        Button btnSubirLogo = new Button("Subir Logo...");
        btnSubirLogo.setGraphic(FontIcon.of(FontAwesomeSolid.UPLOAD, 13, Color.WHITE));
        btnSubirLogo.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnSubirLogo.setPrefHeight(35);

        Button btnEliminarLogo = new Button("Eliminar Logo Custom");
        btnEliminarLogo.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH, 13, Color.WHITE));
        btnEliminarLogo.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnEliminarLogo.setPrefHeight(35);

        Label lblStatusLogo = new Label();
        lblStatusLogo.setFont(Font.font("Arial", 13));

        HBox actionButtons = new HBox(15, btnSubirLogo, btnEliminarLogo, lblStatusLogo);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        logoControls.getChildren().addAll(lblLogoDesc, actionButtons);
        logoContainer.getChildren().addAll(logoPreview, logoControls);

        btnSubirLogo.setOnAction(ev -> subirLogo(currentLogoView, lblStatusLogo));
        btnEliminarLogo.setOnAction(ev -> eliminarLogo(currentLogoView, lblStatusLogo));

        cargarLogoEnUI(currentLogoView, lblStatusLogo);

        root.getChildren().addAll(titulo, toolbar, scroll, sep, tituloMensajes, gridMensajes, sepLogo, tituloLogo, logoContainer);
        cargarImagenes();
        return root;
    }

    private void cargarLogoEnUI(ImageView imageView, Label statusLabel) {
        new Thread(() -> {
            try {
                String logoUrl = "http://" + ConfigManager.getIp() + ":8080/api/config/logo";
                URL url = new URL(logoUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    try (InputStream is = conn.getInputStream()) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                        byte[] bytes = bos.toByteArray();
                        Image img = new Image(new ByteArrayInputStream(bytes));
                        Platform.runLater(() -> {
                            imageView.setImage(img);
                            statusLabel.setText("✅ Logo personalizado activo");
                            statusLabel.setTextFill(Color.web("#27ae60"));
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        imageView.setImage(null);
                        statusLabel.setText("🏢 Sin logo personalizado (usando predeterminado en kiosco)");
                        statusLabel.setTextFill(Color.web("#7f8c8d"));
                    });
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    imageView.setImage(null);
                    statusLabel.setText("⚠️ Error al cargar el logo desde el servidor");
                    statusLabel.setTextFill(Color.web("#d35400"));
                });
            }
        }).start();
    }

    private void subirLogo(ImageView imageView, Label statusLabel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Logo de la Institución");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes PNG/JPG", "*.png", "*.jpg", "*.jpeg"));

        File archivo = chooser.showOpenDialog(new Stage());
        if (archivo == null) return;

        final long MAX_BYTES = 5 * 1024 * 1024L; // 5 MB
        if (archivo.length() > MAX_BYTES) {
            statusLabel.setText("❌ El archivo de logo supera el límite de 5 MB");
            statusLabel.setTextFill(Color.web("#c0392b"));
            return;
        }

        statusLabel.setText("Subiendo logo...");
        statusLabel.setTextFill(Color.web("#7f8c8d"));

        new Thread(() -> {
            try {
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/config/logo");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = conn.getOutputStream();
                     PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                    pw.append("--").append(boundary).append("\r\n");
                    pw.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"logo.png\"\r\n");
                    
                    String contentType = "image/png";
                    String nameLower = archivo.getName().toLowerCase();
                    if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) contentType = "image/jpeg";

                    pw.append("Content-Type: ").append(contentType).append("\r\n\r\n");
                    pw.flush();

                    try (FileInputStream fis = new FileInputStream(archivo)) {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = fis.read(buf)) != -1) os.write(buf, 0, read);
                    }
                    os.flush();

                    pw.append("\r\n--").append(boundary).append("--\r\n");
                    pw.flush();
                }

                final int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) {
                        statusLabel.setText("✅ Logo subido correctamente");
                        statusLabel.setTextFill(Color.web("#27ae60"));
                        cargarLogoEnUI(imageView, statusLabel);
                    } else {
                        statusLabel.setText("❌ Error al subir logo (código " + code + ")");
                        statusLabel.setTextFill(Color.web("#c0392b"));
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Error de conexión: " + ex.getMessage());
                    statusLabel.setTextFill(Color.web("#c0392b"));
                });
            }
        }).start();
    }

    private void eliminarLogo(ImageView imageView, Label statusLabel) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el logo institucional y revertir al predeterminado?",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar eliminación");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/config/logo");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE");
                        final int code = conn.getResponseCode();
                        Platform.runLater(() -> {
                            if (code == 200) {
                                statusLabel.setText("✅ Logo eliminado correctamente");
                                statusLabel.setTextFill(Color.web("#27ae60"));
                                cargarLogoEnUI(imageView, statusLabel);
                            } else {
                                statusLabel.setText("❌ Error al eliminar (código " + code + ")");
                                statusLabel.setTextFill(Color.web("#c0392b"));
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            statusLabel.setText("❌ Error de conexión");
                            statusLabel.setTextFill(Color.web("#c0392b"));
                        });
                    }
                }).start();
            }
        });
    }

    private void cargarImagenes() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/carrusel/lista");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                List<String> nombres = new ObjectMapper().readValue(conn.getInputStream(), new TypeReference<List<String>>() {});
                Platform.runLater(() -> {
                    listaNombres.setAll(nombres);
                    actualizarGaleriaUI();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar imágenes", false));
            }
        }).start();
    }

    private void actualizarGaleriaUI() {
        galeria.getChildren().clear();
        for (int i = 0; i < listaNombres.size(); i++) {
            galeria.getChildren().add(crearTarjetaImagen(listaNombres.get(i), i));
        }
        if (listaNombres.isEmpty()) {
            Label vacio = new Label("No hay imágenes cargadas. Sube una con el botón.");
            vacio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            galeria.getChildren().add(vacio);
        }
    }

    private VBox crearTarjetaImagen(String nombre, int index) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.1),8,0,0,2);");
        card.setPrefWidth(160);

        StackPane previewContainer = new StackPane();
        previewContainer.setPrefSize(220, 160);
        previewContainer.setStyle("-fx-background-color: #eef2f3; -fx-background-radius: 6;");

        ImageView iv = new ImageView();
        iv.setFitWidth(220);
        iv.setFitHeight(160);
        iv.setPreserveRatio(true);

        if (nombre.toLowerCase().endsWith(".mp4")) {
            FontIcon playIcon = FontIcon.of(FontAwesomeSolid.PLAY_CIRCLE, 48, Color.web("#7f8c8d"));
            previewContainer.getChildren().add(playIcon);
        } else {
            previewContainer.getChildren().add(iv);
            new Thread(() -> {
                try {
                    URL imgUrl = new URL("http://" + ConfigManager.getIp() + ":8080/api/carrusel/imagen/" + nombre);
                    Image img = new Image(imgUrl.toString(), 140, 100, true, true);
                    Platform.runLater(() -> iv.setImage(img));
                } catch (Exception ignored) {}
            }).start();
        }

        Label lblNombre = new Label(nombre.length() > 22 ? nombre.substring(0, 19) + "..." : nombre);
        lblNombre.setFont(Font.font("Arial", 11));
        lblNombre.setTextFill(Color.web("#7f8c8d"));

        HBox controles = new HBox(5);
        controles.setAlignment(Pos.CENTER);

        Button btnArriba = new Button();
        btnArriba.setGraphic(FontIcon.of(FontAwesomeSolid.ARROW_UP, 10, Color.web("#34495e")));
        btnArriba.setDisable(index == 0);
        btnArriba.setOnAction(e -> moverImagen(index, -1));

        Button btnAbajo = new Button();
        btnAbajo.setGraphic(FontIcon.of(FontAwesomeSolid.ARROW_DOWN, 10, Color.web("#34495e")));
        btnAbajo.setDisable(index == listaNombres.size() - 1);
        btnAbajo.setOnAction(e -> moverImagen(index, 1));

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setGraphic(FontIcon.of(FontAwesomeSolid.TRASH, 11, Color.WHITE));
        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
        btnEliminar.setOnAction(e -> eliminarImagen(nombre));

        controles.getChildren().addAll(btnArriba, btnAbajo, btnEliminar);

        card.getChildren().addAll(previewContainer, lblNombre, controles);
        card.setPrefWidth(200);
        return card;
    }

    private void moverImagen(int fromIndex, int offset) {
        int toIndex = fromIndex + offset;
        if (toIndex >= 0 && toIndex < listaNombres.size()) {
            String item = listaNombres.remove(fromIndex);
            listaNombres.add(toIndex, item);
            actualizarGaleriaUI();
        }
    }

    private void guardarOrden() {
        if (listaNombres.isEmpty()) return;
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/carrusel/orden");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                new ObjectMapper().writeValue(conn.getOutputStream(), listaNombres);
                
                int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) mostrarMensaje("✅ Orden guardado correctamente", true);
                    else mostrarMensaje("Error al guardar orden", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
            }
        }).start();
    }

    private void subirImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar Imagen o Video");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Multimedia", "*.jpg", "*.jpeg", "*.png", "*.mp4"));

        File archivo = chooser.showOpenDialog(new Stage());
        if (archivo == null) return;

        // Validar tamaño (máximo 100 MB)
        final long MAX_BYTES = 100 * 1024 * 1024L;
        if (archivo.length() > MAX_BYTES) {
            mostrarMensaje("❌ El archivo supera el tamaño máximo permitido (100 MB). Tamaño actual: "
                    + String.format("%.1f", archivo.length() / (1024.0 * 1024.0)) + " MB", false);
            return;
        }

        new Thread(() -> {
            try {
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/carrusel/subir");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = conn.getOutputStream();
                     PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

                    pw.append("--").append(boundary).append("\r\n");
                    pw.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"")
                      .append(archivo.getName()).append("\"\r\n");
                    
                    String contentType = "application/octet-stream";
                    String nameLower = archivo.getName().toLowerCase();
                    if (nameLower.endsWith(".png")) contentType = "image/png";
                    else if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) contentType = "image/jpeg";
                    else if (nameLower.endsWith(".mp4")) contentType = "video/mp4";

                    pw.append("Content-Type: ").append(contentType).append("\r\n\r\n");
                    pw.flush();

                    try (FileInputStream fis = new FileInputStream(archivo)) {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = fis.read(buf)) != -1) os.write(buf, 0, read);
                    }
                    os.flush();

                    pw.append("\r\n--").append(boundary).append("--\r\n");
                    pw.flush();
                }

                final int code = conn.getResponseCode();
                Platform.runLater(() -> {
                    if (code == 200) { mostrarMensaje("✅ Imagen subida", true); cargarImagenes(); }
                    else mostrarMensaje("Error al subir imagen (código " + code + ")", false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarMensaje("Error de conexión: " + ex.getMessage(), false));
            }
        }).start();
    }

    private void eliminarImagen(String nombre) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la imagen \"" + nombre + "\"?\nEsta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar eliminación");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/carrusel/eliminar/" + nombre);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE");
                        final int code = conn.getResponseCode();
                        Platform.runLater(() -> {
                            if (code == 200) { mostrarMensaje("✅ Imagen eliminada", true); cargarImagenes(); }
                            else mostrarMensaje("Error al eliminar", false);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> mostrarMensaje("Error de conexión", false));
                    }
                }).start();
            }
        });
    }

    private void mostrarMensaje(String msg, boolean ok) {
        lblMensaje.setText(msg);
        lblMensaje.setTextFill(ok ? Color.web("#27ae60") : Color.web("#c0392b"));
    }
}
