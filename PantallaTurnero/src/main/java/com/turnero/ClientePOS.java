package com.turnero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.print.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientePOS extends Application {

    private String categoriaSeleccionada = null;
    private Label resultadoLabel = new Label();
    private List<Categoria> listaCategorias = new ArrayList<>();
    private Stage primaryStage;
    private VBox categoriasContainer;
    private ScrollPane scrollPane;

    // Colores como constantes
    private final String COLOR_PRINCIPAL = "#2980b9";
    private final String COLOR_SECUNDARIO = "#3498db";
    private final String COLOR_EXITO = "#27ae60";
    private final String COLOR_ADVERTENCIA = "#f39c12";
    private final String COLOR_ERROR = "#e74c3c";
    private final String COLOR_FONDO = "#ecf0f1";
    private final String COLOR_NEUTRO = "#7f8c8d";

    // Paleta de colores complementarios que armonizan con #2980b9
    private final String[] COLORES_CATEGORIAS = {
            "#2980b9", // Azul principal - color representativo
            "#27ae60", // Verde esmeralda - complementario natural
            "#8e44ad", // Púrpura elegante - análogo
            "#e67e22", // Naranja vibrante - complementario cálido
            "#16a085", // Turquesa - análogo frío
            "#c0392b", // Rojo carmesí - triádico
            "#f39c12", // Amarillo dorado - complementario split
            "#2c3e50", // Azul marino - tono oscuro
            "#d35400", // Naranja quemado - cálido profundo
            "#7f8c8d", // Gris acero - neutro elegante
            "#9b59b6", // Lavanda - análogo suave
            "#1abc9c", // Aguamarina - refrescante
            "#e74c3c", // Rojo coral - vibrante
            "#34495e", // Gris azulado - sofisticado
            "#f1c40f"  // Amarillo brillante - energético
    };

    @Override
    public void start(Stage stage) {
        if (ConfigManager.getIp() == null) {
            ConfigWindow cw = new ConfigWindow();
            cw.start(new Stage());
            return;
        }
        this.primaryStage = stage;

        // Configurar pantalla completa
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // Ocultar mensaje de salida
        stage.setResizable(false);

        // Logo responsivo
        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(600);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }

        Label subtitulo = new Label("Seleccione el tipo de servicio que necesita");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 28));
        subtitulo.setTextFill(Color.web("#7f8c8d"));
        subtitulo.setWrapText(true);
        subtitulo.setTextAlignment(TextAlignment.CENTER);

        // Contenedor de categorías que se adapta verticalmente
        categoriasContainer = new VBox();
        categoriasContainer.setAlignment(Pos.CENTER);
        categoriasContainer.setSpacing(25);
        categoriasContainer.setPadding(new Insets(30));

        // ScrollPane para manejar contenido que exceda la pantalla
        scrollPane = new ScrollPane(categoriasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        cargarCategoriasDinamicamente();

        // Label de resultado responsivo
        resultadoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        resultadoLabel.setTextFill(Color.web(COLOR_EXITO));
        resultadoLabel.setTextAlignment(TextAlignment.CENTER);
        resultadoLabel.setWrapText(true);
        resultadoLabel.setPadding(new Insets(30));

        // Contenedor principal
        VBox headerBox = new VBox(20, logoView, subtitulo);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(30));

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + COLOR_FONDO + ";");

        // Configurar el layout principal para ser completamente responsivo
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        root.getChildren().addAll(headerBox, scrollPane, resultadoLabel);

        Scene scene = new Scene(root);

        // Listeners para responsividad completa
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            ajustarResponsividad(newVal.doubleValue(), scene.getHeight());
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            ajustarResponsividad(scene.getWidth(), newVal.doubleValue());
        });

        stage.setScene(scene);
        stage.setTitle("Sistema de Turnos - Kiosko");
        stage.show();

        // Ajuste inicial
        ajustarResponsividad(scene.getWidth(), scene.getHeight());
    }

    private void ajustarResponsividad(double ancho, double alto) {
        // Determinar si es orientación vertical o horizontal
        boolean esVertical = alto > ancho;

        // Ajustar tamaños de fuente según el tamaño de pantalla
        double factorEscala = Math.min(ancho, alto) / 1000.0;
        factorEscala = Math.max(0.7, Math.min(2.0, factorEscala));

        // Ajustar logo y subtítulo
        try {
            VBox headerBox = (VBox) ((VBox) primaryStage.getScene().getRoot()).getChildren().get(0);

            // Si el índice 0 es el ImageView (Logo)
            if (headerBox.getChildren().get(0) instanceof javafx.scene.image.ImageView) {
                javafx.scene.image.ImageView logoView = (javafx.scene.image.ImageView) headerBox.getChildren().get(0);
                logoView.setFitWidth(600 * factorEscala);

                Label subtitulo = (Label) headerBox.getChildren().get(1);
                subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 28 * factorEscala));
            } else {
                Label subtitulo = (Label) headerBox.getChildren().get(0);
                subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 28 * factorEscala));
            }
        } catch (Exception e) {
            // Ignorar errores de casting durante la inicialización
        }

        // Ajustar label de resultado
        resultadoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32 * factorEscala));

        // Recargar categorías con nuevo layout
        cargarCategoriasDinamicamente();

        // Configurar el contenedor según la orientación
        if (esVertical) {
            categoriasContainer.setSpacing(20);
            categoriasContainer.setPadding(new Insets(20));
        } else {
            categoriasContainer.setSpacing(25);
            categoriasContainer.setPadding(new Insets(30));
        }
    }

    private String obtenerColorComplementario(int indice) {
        // Usar el índice para seleccionar un color de la paleta complementaria
        return COLORES_CATEGORIAS[indice % COLORES_CATEGORIAS.length];
    }

    private void cargarCategoriasDinamicamente() {
        try {
            // Solo solicitamos a la API si la lista está vacía, para no saturar al servidor
            // con repeticiones continuas cada vez que la pantalla (animación o el layout) se acomoda.
            if (listaCategorias.isEmpty()) {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/lista");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (InputStream input = conn.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    Categoria[] categorias = mapper.readValue(input, Categoria[].class);

                    listaCategorias.clear();
                    listaCategorias.addAll(Arrays.asList(categorias));
                } finally {
                    conn.disconnect();
                }
            }

            // Limpiar contenedor
            categoriasContainer.getChildren().clear();

            // Determinar el layout según el tamaño de pantalla
            double ancho = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : 1920;
            double alto = primaryStage.getScene() != null ? primaryStage.getScene().getHeight() : 1080;

            boolean esVertical = alto > ancho;
            boolean esPantallaChica = ancho < 800 || alto < 600;

            if (esVertical || esPantallaChica) {
                crearLayoutVertical();
            } else {
                crearLayoutHorizontal(ancho);
            }

        } catch (Exception e) {
            resultadoLabel.setText("❌ Error al cargar categorías del servidor");
            resultadoLabel.setTextFill(Color.web(COLOR_ERROR));
            e.printStackTrace();
        }
    }

    private void crearLayoutVertical() {
        for (int i = 0; i < listaCategorias.size(); i++) {
            Categoria cat = listaCategorias.get(i);
            String nombre = cat.getNombre();
            String color = obtenerColorComplementario(i);
            Button btn = crearBotonCategoriaVertical(nombre, color);
            categoriasContainer.getChildren().add(btn);
        }
    }

    private void crearLayoutHorizontal(double anchoDisponible) {
        // Cálculo dinámico de categorías por fila
        int totalCategorias = listaCategorias.size();
        int categoriasPerFila = calcularCategoriasPerFila(anchoDisponible, totalCategorias);

        HBox filaActual = null;
        int contador = 0;

        for (int i = 0; i < listaCategorias.size(); i++) {
            if (contador % categoriasPerFila == 0) {
                filaActual = new HBox();
                filaActual.setAlignment(Pos.CENTER);
                filaActual.setSpacing(30);
                categoriasContainer.getChildren().add(filaActual);
            }

            Categoria cat = listaCategorias.get(i);
            String nombre = cat.getNombre();
            String color = obtenerColorComplementario(i);
            Button btn = crearBotonCategoriaHorizontal(nombre, color, categoriasPerFila);
            filaActual.getChildren().add(btn);

            contador++;
        }
    }

    private int calcularCategoriasPerFila(double anchoDisponible, int totalCategorias) {
        // Ancho mínimo por botón (incluyendo spacing)
        double anchoMinimoBoton = 280; // 250px botón + 30px spacing

        // Calcular máximo de categorías que caben por fila
        int maxPorFila = (int) Math.floor(anchoDisponible * 0.8 / anchoMinimoBoton);
        maxPorFila = Math.max(1, maxPorFila); // Mínimo 1 por fila

        // Si hay pocas categorías, usar menos columnas para mejor distribución
        if (totalCategorias <= 3) {
            return Math.min(totalCategorias, maxPorFila);
        } else if (totalCategorias <= 6) {
            return Math.min(3, maxPorFila);
        } else if (totalCategorias <= 12) {
            return Math.min(4, maxPorFila);
        } else {
            // Para muchas categorías, usar el máximo posible
            return Math.min(5, maxPorFila);
        }
    }

    private Button crearBotonCategoriaVertical(String nombre, String color) {
        Button boton = new Button(nombre.toUpperCase());

        double ancho = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : 1920;
        double factorEscala = Math.min(ancho / 600.0, 2.0);

        double anchoBoton = Math.min(ancho * 0.8, 800);
        double altoBoton = 120 * factorEscala;

        // Calcular tamaño de fuente adaptativo
        int tamanoFuente = calcularTamanoFuenteAdaptativo(nombre, anchoBoton, altoBoton);

        boton.setFont(Font.font("Arial", FontWeight.BOLD, tamanoFuente));
        boton.setPrefWidth(anchoBoton);
        boton.setPrefHeight(altoBoton);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setWrapText(true); // Permitir salto de línea si es necesario

        aplicarEstiloBoton(boton, color);
        boton.setOnAction(e -> seleccionarCategoria(nombre));

        return boton;
    }

    private Button crearBotonCategoriaHorizontal(String nombre, String color, int categoriasPerFila) {
        Button boton = new Button(nombre.toUpperCase());

        double ancho = primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : 1920;
        double factorEscala = Math.min(ancho / 1200.0, 1.5);

        // Calcular ancho dinámicamente según categorías por fila
        double anchoBoton = (ancho * 0.8) / categoriasPerFila - 30;
        anchoBoton = Math.max(250, Math.min(400, anchoBoton)); // Entre 250px y 400px
        double altoBoton = 140 * factorEscala;

        // Calcular tamaño de fuente adaptativo
        int tamanoFuente = calcularTamanoFuenteAdaptativo(nombre, anchoBoton, altoBoton);

        boton.setFont(Font.font("Arial", FontWeight.BOLD, tamanoFuente));
        boton.setPrefSize(anchoBoton, altoBoton);
        boton.setWrapText(true); // Permitir salto de línea si es necesario

        aplicarEstiloBoton(boton, color);
        boton.setOnAction(e -> seleccionarCategoria(nombre));

        return boton;
    }

    private int calcularTamanoFuenteAdaptativo(String texto, double anchoBoton, double altoBoton) {
        // Calcular tamaño base según dimensiones del botón
        int tamanoBase = (int) Math.min(anchoBoton / 12, altoBoton / 4);

        // Ajustar según longitud del texto
        int longitudTexto = texto.length();

        if (longitudTexto <= 6) {
            return Math.max(20, tamanoBase); // Texto corto
        } else if (longitudTexto <= 12) {
            return Math.max(18, (int)(tamanoBase * 0.9)); // Texto medio
        } else if (longitudTexto <= 18) {
            return Math.max(16, (int)(tamanoBase * 0.8)); // Texto largo
        } else {
            return Math.max(14, (int)(tamanoBase * 0.7)); // Texto muy largo
        }
    }

    private void aplicarEstiloBoton(Button boton, String color) {
        String estiloBase = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                        "-fx-cursor: hand; " +
                        "-fx-text-alignment: center;",
                color
        );

        boton.setStyle(estiloBase);

        boton.setOnMouseEntered(e -> {
            boton.setStyle(estiloBase.replace(color, darkenColor(color, 0.8)));
            boton.setScaleX(1.05);
            boton.setScaleY(1.05);
        });

        boton.setOnMouseExited(e -> {
            boton.setStyle(estiloBase);
            boton.setScaleX(1.0);
            boton.setScaleY(1.0);
        });
    }

    private String darkenColor(String hexColor, double factor) {
        try {
            Color color = Color.web(hexColor);
            Color darkerColor = color.deriveColor(0, 1, factor, 1);
            return String.format("#%02X%02X%02X",
                    (int)(darkerColor.getRed() * 255),
                    (int)(darkerColor.getGreen() * 255),
                    (int)(darkerColor.getBlue() * 255));
        } catch (Exception e) {
            return hexColor;
        }
    }

    private void mostrarModalPreferencia() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY); // Solo muestra la X para cerrar
        dialog.setTitle(""); // Sin título
        dialog.setResizable(false);

        // Calcular tamaño responsivo del modal
        double pantallAncho = primaryStage.getScene().getWidth();
        double pantallaAlto = primaryStage.getScene().getHeight();

        double modalAncho = Math.min(700, pantallAncho * 0.6);
        double modalAlto = Math.min(500, pantallaAlto * 0.7);
        double factorEscala = Math.min(modalAncho / 700.0, modalAlto / 500.0);

        VBox contenidoPrincipal = new VBox(20 * factorEscala);
        contenidoPrincipal.setAlignment(Pos.CENTER);
        contenidoPrincipal.setPadding(new Insets(30 * factorEscala));
        contenidoPrincipal.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15;"
        );

        // Título con icono
        Label titulo = new Label("¿Necesita Turno Preferencial?");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, (int)(28 * factorEscala)));
        titulo.setTextFill(Color.web(COLOR_PRINCIPAL));
        titulo.setTextAlignment(TextAlignment.CENTER);
        titulo.setWrapText(true);

        // Contenedor de iconos para preferencias (solo iconos, sin texto)
        HBox iconosContainer = new HBox(25 * factorEscala);
        iconosContainer.setAlignment(Pos.CENTER);
        iconosContainer.setPadding(new Insets(15 * factorEscala));

        // Crear iconos usando caracteres Unicode
        Label iconoAdultoMayor = crearIconoPreferencia("👴", factorEscala);
        Label iconoDiscapacidad = crearIconoPreferencia("♿", factorEscala);
        Label iconoEmbarazada = crearIconoPreferencia("🤰", factorEscala);
        Label iconoNinos = crearIconoPreferencia("👶", factorEscala);

        iconosContainer.getChildren().addAll(iconoAdultoMayor, iconoDiscapacidad, iconoEmbarazada, iconoNinos);

        // Mensaje de advertencia centrado en color neutro
        Label advertencia = new Label("El uso indebido resultará en cancelación del turno");
        advertencia.setFont(Font.font("Arial", FontWeight.BOLD, (int)(16 * factorEscala)));
        advertencia.setTextFill(Color.web(COLOR_NEUTRO));
        advertencia.setTextAlignment(TextAlignment.CENTER);
        advertencia.setAlignment(Pos.CENTER); // Centrar completamente
        advertencia.setWrapText(true);
        advertencia.setMaxWidth(modalAncho * 0.9);

        // Botones con iconos únicamente (sin cancelar)
        Button btnSi = new Button("✓ SÍ");
        Button btnNo = new Button("✗ NO");

        configurarBotonModal(btnSi, COLOR_ADVERTENCIA, (int)(22 * factorEscala), factorEscala);
        configurarBotonModal(btnNo, COLOR_PRINCIPAL, (int)(22 * factorEscala), factorEscala);

        btnSi.setOnAction(e -> {
            dialog.close();
            sacarTurno(true);
        });

        btnNo.setOnAction(e -> {
            dialog.close();
            sacarTurno(false);
        });

        // Contenedor de botones en fila (solo SÍ y NO)
        HBox botonesBox = new HBox(30 * factorEscala, btnSi, btnNo);
        botonesBox.setAlignment(Pos.CENTER);

        contenidoPrincipal.getChildren().addAll(titulo, iconosContainer, advertencia, botonesBox);

        // Crear fondo semi-transparente que permite cerrar al hacer clic fuera
        StackPane fondo = new StackPane();
        fondo.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Agregar evento para cerrar al hacer clic fuera del modal
        fondo.setOnMouseClicked(e -> {
            if (e.getTarget() == fondo) {
                dialog.close();
                // Limpiar selección al cerrar
                categoriaSeleccionada = null;
                resultadoLabel.setText("");
            }
        });

        fondo.getChildren().add(contenidoPrincipal);

        Scene scene = new Scene(fondo, modalAncho, modalAlto);
        dialog.setScene(scene);

        // Configurar cierre automático al cerrar con X
        dialog.setOnCloseRequest(e -> {
            categoriaSeleccionada = null;
            resultadoLabel.setText("");
        });

        dialog.centerOnScreen();
        dialog.show();
    }

    private Label crearIconoPreferencia(String icono, double factorEscala) {
        Label label = new Label(icono);
        label.setFont(Font.font("Arial", (int)(60 * factorEscala)));
        label.setStyle(
                "-fx-background-color: " + COLOR_FONDO + "; " +
                        "-fx-background-radius: " + (40 * factorEscala) + "; " +
                        "-fx-padding: " + (15 * factorEscala) + "; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        label.setAlignment(Pos.CENTER);
        label.setPrefSize(90 * factorEscala, 90 * factorEscala);
        return label;
    }

    private void configurarBotonModal(Button boton, String color, int fontSize, double factorEscala) {
        boton.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
        boton.setPrefSize(150 * factorEscala, 60 * factorEscala);

        String estiloBase = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2); " +
                        "-fx-cursor: hand;",
                color
        );

        boton.setStyle(estiloBase);

        boton.setOnMouseEntered(e -> {
            boton.setStyle(estiloBase.replace(color, darkenColor(color, 0.8)));
            boton.setScaleX(1.05);
            boton.setScaleY(1.05);
        });

        boton.setOnMouseExited(e -> {
            boton.setStyle(estiloBase);
            boton.setScaleX(1.0);
            boton.setScaleY(1.0);
        });
    }

    private void seleccionarCategoria(String nombreCategoria) {
        this.categoriaSeleccionada = nombreCategoria;
        resultadoLabel.setText("Procesando solicitud...");
        resultadoLabel.setTextFill(Color.web(COLOR_ADVERTENCIA));
        mostrarModalPreferencia();
    }

    private void sacarTurno(boolean preferente) {
        try {
            Categoria categoria = listaCategorias.stream()
                    .filter(c -> c.getNombre().equals(categoriaSeleccionada))
                    .findFirst()
                    .orElse(null);

            if (categoria == null) {
                resultadoLabel.setText("❌ Error: Categoría no encontrada");
                resultadoLabel.setTextFill(Color.web(COLOR_ERROR));
                return;
            }

            Map<String, Object> jsonMap = new LinkedHashMap<>();
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", categoria.getId());
            jsonMap.put("categoria", catMap);
            jsonMap.put("preferente", preferente);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(jsonMap);

            URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(json.getBytes());

            String numeroTurno;
            String tipoTurno = preferente ? "PREFERENCIAL" : "REGULAR";
            Turno turno;

            try (InputStream input = conn.getInputStream()) {
                turno = mapper.readValue(input, Turno.class);
                numeroTurno = turno.getNumero();
            } finally {
                conn.disconnect();
            }

            resultadoLabel.setText(
                    String.format("🎫 ¡Turno Generado Exitosamente!\n\n" +
                                    "Número: %s\n" +
                                    "Tipo: %s\n" +
                                    "Categoría: %s\n\n" +
                                    "Por favor, conserve su ticket impreso",
                            numeroTurno, tipoTurno, categoriaSeleccionada)
            );
            resultadoLabel.setTextFill(Color.web(COLOR_EXITO));

            imprimirTicket(turno);
            reproducirBeep();

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    javafx.application.Platform.runLater(() -> {
                        categoriaSeleccionada = null;
                        resultadoLabel.setText("");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            resultadoLabel.setText("❌ Error al generar el turno\nIntente nuevamente");
            resultadoLabel.setTextFill(Color.web(COLOR_ERROR));
            e.printStackTrace();
        }
    }

    private void imprimirTicket(Turno turno) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(new byte[]{0x1B, 0x40});
            outputStream.write(new byte[]{0x1B, 0x61, 0x01});

            outputStream.write("Sistema de Turnos\n".getBytes("CP437"));
            outputStream.write("================================\n".getBytes("CP437"));

            outputStream.write(new byte[]{0x1D, 0x21, 0x01});
            outputStream.write("SU TURNO ES\n".getBytes("CP437"));
            outputStream.write(new byte[]{0x1D, 0x21, 0x00});

            outputStream.write(new byte[]{0x1D, 0x21, 0x33});
            outputStream.write((turno.getNumero() + "\n").getBytes("CP437"));
            outputStream.write(new byte[]{0x1D, 0x21, 0x00});

            String categoriaNombre = turno.getCategoria().getNombre();
            outputStream.write(("Categoria: " + categoriaNombre + "\n").getBytes("CP437"));

            if (turno.getNumero().contains("P")) {
                outputStream.write("TURNO PREFERENCIAL\n".getBytes("CP437"));
            }

            outputStream.write("\n================================\n".getBytes("CP437"));

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            outputStream.write(("Emitido: " + fecha + "\n").getBytes("CP437"));

            outputStream.write("\nConserve este ticket\n".getBytes("CP437"));
            outputStream.write("Gracias por su visita\n\n".getBytes("CP437"));
            outputStream.write("\n\n\n".getBytes("CP437"));

            outputStream.write(new byte[]{0x1D, 0x56, 0x00});

            byte[] bytes = outputStream.toByteArray();
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();

            if (service != null) {
                Doc doc = new SimpleDoc(bytes, flavor, null);
                DocPrintJob job = service.createPrintJob();
                job.print(doc, null);
            } else {
                System.out.println("No se encontró impresora disponible.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reproducirBeep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static void main(String[] args) {
        if (ConfigManager.getIp() == null) {
            Application.launch(ConfigWindow.class, args);
        } else {
            launch(args);
        }
    }
}