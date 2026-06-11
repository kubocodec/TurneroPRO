package com.turnero;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PanelAdmin extends Application {

    // Componentes principales
    private Stage primaryStage;
    private Label lblTurnoActual;
    private Label lblCategoria;
    private Label lblEstado;
    private Label lblHora;
    private ComboBox<Integer> comboPuestos;
    private Button btnLlamarTurno;
    private Button btnReiniciarTurnos;
    private ProgressIndicator loadingIndicator;
    private VBox statsContainer;

    // Variables de estado
    private Long idTurnoActual = null;
    private ScheduledExecutorService scheduler;
    private TableView<TurnoDoble> tablaUnificada;

    private ComboBox<Categoria> comboCategorias;
    private ComboBox<String> comboTipo;


    @Override
    public void start(Stage stage) {
        if (ConfigManager.getIp() == null) {
            ConfigWindow cw = new ConfigWindow();
            cw.start(new Stage());
            return;
        }

        if (UsuarioLogeado.obtenerUsuario() == null) {
            LoginView login = new LoginView();
            login.start(new Stage());
            return;
        }
        this.primaryStage = stage;

        String rol = UsuarioLogeado.obtenerUsuario().getRol();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);

        stage.setTitle("Sistema de Administración de Turnos - Panel de Control");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double width  = Math.max(900, screenBounds.getWidth() * 0.85);
        double height = Math.max(600, screenBounds.getHeight() * 0.85);

        javafx.scene.Node contenido;

        if (esAdmin) {
            // ADMIN: TabPane con 4 pestañas
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setStyle("-fx-tab-min-height: 40px; -fx-font-size: 14px;");

            Tab tabTurnos = new Tab("Turnos", createMainLayout());
            Tab tabUsers  = new Tab("Usuarios", new ScrollPane(new VistaUsuarios().construir()));
            Tab tabCats   = new Tab("Categorías", new ScrollPane(new VistaCategorias().construir()));
            Tab tabCarr   = new Tab("Carrusel", new ScrollPane(new VistaCarrusel().construir()));
            
            tabTurnos.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLIPBOARD_LIST, 16));
            tabUsers.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.USERS, 16));
            tabCats.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FOLDER_OPEN, 16));
            tabCarr.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.IMAGES, 16));
            
            tabPane.setTabMinWidth(80);

            tabTurnos.setStyle("-fx-background-color: #27ae60;");

            tabPane.getTabs().addAll(tabTurnos, tabUsers, tabCats, tabCarr);
            contenido = tabPane;
        } else {
            // USER: solo vista de turnos
            contenido = createMainLayout();
        }

        Scene scene = new Scene((javafx.scene.Parent) contenido, width, height);
        scene.setFill(Color.web("#f8f9fa"));

        scene.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        stage.setScene(scene);
        stage.centerOnScreen();
        
        stage.setOnCloseRequest(e -> {
            stop();
            Platform.exit();
            System.exit(0);
        });
        
        stage.show();

        inicializarSistema();
        configurarActualizacionesAutomaticas();
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Contenido principal
        HBox mainContent = createMainContent();
        root.setCenter(mainContent);

        // Footer con información del sistema
        HBox footer = createFooter();
        root.setBottom(footer);

        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #2980b9, #3498db);" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
        );

        // Fila superior: título + botón cerrar sesión
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("SISTEMA DE ADMINISTRACIÓN DE TURNOS");
        titulo.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BUILDING, 24, Color.WHITE));
        titulo.setWrapText(true);
        titulo.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 1, 1);"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Info del usuario logueado
        Usuario u = UsuarioLogeado.obtenerUsuario();
        String nombreUsuario = u != null ? u.getNombre() + " (" + u.getRol() + ")" : "";
        Label lblUsuario = new Label("Usuario: " + nombreUsuario);
        lblUsuario.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.USER, 14, Color.WHITE));
        lblUsuario.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px;");

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SIGN_OUT_ALT, 14, Color.WHITE));
        btnLogout.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 8;"
        );
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
                "-fx-background-color: rgba(255,255,255,0.35);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: white;" +
                "-fx-border-radius: 8;"
        ));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 8;"
        ));
        btnLogout.setOnAction(e -> cerrarSesion());

        HBox userBox = new HBox(12, lblUsuario, btnLogout);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(titulo, spacer, userBox);

        // Fila inferior: subtítulo + hora
        HBox subtituloContainer = new HBox();
        subtituloContainer.setAlignment(Pos.CENTER_LEFT);
        subtituloContainer.setSpacing(20);

        Label subtitulo = new Label("Panel de Control Administrativo");
        subtitulo.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: rgba(255,255,255,0.9);"
        );

        lblHora = new Label();
        lblHora.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);" +
                        "-fx-font-family: 'Courier New';"
        );
        actualizarHora();

        subtituloContainer.getChildren().addAll(subtitulo, new Region(), lblHora);
        HBox.setHgrow(subtituloContainer.getChildren().get(1), Priority.ALWAYS);

        header.getChildren().addAll(topRow, subtituloContainer);
        return header;
    }

    private void cerrarSesion() {
        if (scheduler != null) scheduler.shutdown();
        UsuarioLogeado.cerrarSesion();
        Stage currentStage = primaryStage;
        try {
            LoginView login = new LoginView();
            login.start(new Stage());
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private HBox createMainContent() {
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Panel izquierdo - Control de turnos
        VBox leftPanel = createControlPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMinWidth(180);

        // Panel central - Información actual
        VBox centerPanel = createCurrentTurnPanel();
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        centerPanel.setMinWidth(200);

        // Panel derecho - Estadísticas
        VBox rightPanel = createStatsPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setMinWidth(180);

        mainContent.getChildren().addAll(leftPanel, centerPanel, rightPanel);

        return mainContent;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(20);
        panel.setMinWidth(250);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );

        // Título del panel
        Label titulo = new Label("CONTROL DE TURNOS");
        titulo.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SLIDERS_H, 18, Color.web("#2c3e50")));
        titulo.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;"
        );

        // Separador
        Separator separador = new Separator();
        separador.setStyle("-fx-background-color: #ecf0f1;");

        // Selección de puesto
        VBox puestoContainer = new VBox(10);
        Label lblPuesto = new Label("Seleccionar Puesto de Atención:");
        lblPuesto.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.MAP_MARKER_ALT, 14, Color.web("#34495e")));
        lblPuesto.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #34495e;"
        );

        comboPuestos = new ComboBox<>();
        comboPuestos.getItems().addAll(1, 2, 3, 4, 5);
        comboPuestos.setValue(1);
        comboPuestos.setMaxWidth(Double.MAX_VALUE);
        comboPuestos.setPrefHeight(40);
        comboPuestos.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;"
        );

        puestoContainer.getChildren().addAll(lblPuesto, comboPuestos);

        // Sección de categoría
        VBox categoriaContainer = new VBox(10);
        Label lblCategoria = new Label("Seleccionar Categoría:");
        lblCategoria.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FOLDER_OPEN, 14, Color.web("#34495e")));
        lblCategoria.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #34495e;"
        );

        comboCategorias = new ComboBox<>();
        comboCategorias.setMaxWidth(Double.MAX_VALUE);
        comboCategorias.setPrefHeight(40);
        comboCategorias.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;"
        );

        categoriaContainer.getChildren().addAll(lblCategoria, comboCategorias);

// Sección de tipo
        VBox tipoContainer = new VBox(10);
        Label lblTipo = new Label("Tipo:");
        lblTipo.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.RECEIPT, 14, Color.web("#34495e")));
        lblTipo.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #34495e;"
        );

        comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("General", "Preferencial");
        comboTipo.setValue("General");
        comboTipo.setMaxWidth(Double.MAX_VALUE);
        comboTipo.setPrefHeight(40);
        comboTipo.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;"
        );

        tipoContainer.getChildren().addAll(lblTipo, comboTipo);


        // Botones de acción
        VBox botonesContainer = new VBox(15);

        String rolUsuario = UsuarioLogeado.obtenerUsuario() != null
                ? UsuarioLogeado.obtenerUsuario().getRol() : "";
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rolUsuario);

        // Botón llamar turno (todos los roles)
        btnLlamarTurno = createStyledButton("LLAMAR SIGUIENTE TURNO", "#27ae60", "#2ecc71");
        btnLlamarTurno.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BULLHORN, 14, Color.WHITE));
        btnLlamarTurno.setOnAction(e -> llamarSiguienteTurno());

        // Botón re-llamar turno (todos los roles)
        Button btnReLlamarTurno = createStyledButton("RE-LLAMAR TURNO", "#8e44ad", "#9b59b6");
        btnReLlamarTurno.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.BELL, 14, Color.WHITE));
        btnReLlamarTurno.setOnAction(e -> reLlamarTurnoActual());

        botonesContainer.getChildren().addAll(btnLlamarTurno, btnReLlamarTurno);

        // Botón reiniciar turnos (solo ADMIN)
        if (esAdmin) {
            btnReiniciarTurnos = createStyledButton("REINICIAR TURNOS", "#c0392b", "#e74c3c");
            btnReiniciarTurnos.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.REDO, 14, Color.WHITE));
            btnReiniciarTurnos.setOnAction(e -> reiniciarTurnos());
            botonesContainer.getChildren().add(btnReiniciarTurnos);
        }

        // Indicador de carga
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(30, 30);
        loadingIndicator.setVisible(false);

        //panel.getChildren().addAll(titulo, separador, puestoContainer, botonesContainer, loadingIndicator);
        panel.getChildren().addAll(titulo, separador, puestoContainer, categoriaContainer, tipoContainer, botonesContainer, loadingIndicator);


        return panel;
    }

    private void cargarCategoriasDesdeAPI() {
        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/categorias/lista");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    Categoria[] categorias = mapper.readValue(conn.getInputStream(), Categoria[].class);

                    Platform.runLater(() -> {
                        comboCategorias.getItems().clear();
                        comboCategorias.getItems().addAll(categorias);
                        if (categorias.length > 0) {
                            comboCategorias.setValue(categorias[0]);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> mostrarAlerta("Error", "No se pudo cargar las categorías"));
                e.printStackTrace();
            }
        }).start();
    }


    private TableView<TurnoDoble> createStyledTable() {
        TableView<TurnoDoble> tabla = new TableView<>();
        tabla.setPrefHeight(300);
        tabla.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 13px;"
        );

        // Estilo para el encabezado de la tabla
        tabla.getStylesheets().add(getClass().getResource("/styles/table-styles.css").toExternalForm());

        // Columnas con mejor estilo
        TableColumn<TurnoDoble, String> colGeneral = new TableColumn<>("General");
        colGeneral.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGeneral()));
        colGeneral.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5px;");

        TableColumn<TurnoDoble, String> colPreferencial = new TableColumn<>("Preferencial");
        colPreferencial.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPreferencial()));
        colPreferencial.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5px;");

        // Hacer que las columnas se repartan el ancho disponible
        colGeneral.prefWidthProperty().bind(tabla.widthProperty().multiply(0.48));
        colPreferencial.prefWidthProperty().bind(tabla.widthProperty().multiply(0.48));

        // Personalización de las celdas
        colGeneral.setCellFactory(column -> new TableCell<TurnoDoble, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8px 12px; -fx-border-radius: 4px;");

                    // Si no es la última fila, agregar margen inferior
                    if (getIndex() < getTableView().getItems().size() - 1) {
                        setStyle(getStyle() + "-fx-border-width: 0 0 1px 0; -fx-border-color: #e9ecef;");
                    }
                }
            }
        });

        colPreferencial.setCellFactory(column -> new TableCell<TurnoDoble, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Color ligeramente diferente para distinguir de la columna general
                    setStyle("-fx-background-color: #e8f4f8; -fx-padding: 8px 12px; -fx-border-radius: 4px;");

                    // Si no es la última fila, agregar margen inferior
                    if (getIndex() < getTableView().getItems().size() - 1) {
                        setStyle(getStyle() + "-fx-border-width: 0 0 1px 0; -fx-border-color: #e9ecef;");
                    }
                }
            }
        });

        tabla.getColumns().addAll(colGeneral, colPreferencial);

        // Eliminar líneas de cuadrícula vertical
       // tabla.setGridLinesVisible(false);

        // Estilo para filas vacías
        tabla.setPlaceholder(new Label("No hay turnos disponibles") {{
            setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
        }});

        return tabla;
    }


private VBox createCurrentTurnPanel() {
    VBox panel = new VBox(20);
    panel.setPadding(new Insets(25));
    panel.setAlignment(Pos.CENTER);
    panel.setStyle(
            "-fx-background-color: white;" +
                    "-fx-background-radius: 15px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
    );

    // Título del panel
    Label titulo = new Label("TURNO ACTUAL");
    titulo.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CROSSHAIRS, 18, Color.web("#2c3e50")));
    titulo.setStyle(
            "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2c3e50;"
    );

    // Contenedor del turno actual
    VBox turnoContainer = new VBox(15);
    turnoContainer.setAlignment(Pos.CENTER);
    turnoContainer.setPadding(new Insets(30));
    turnoContainer.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);" +
                    "-fx-background-radius: 12px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 10, 0, 0, 3);"
    );

    lblTurnoActual = new Label("---");
    lblTurnoActual.setStyle(
            "-fx-font-size: 48px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: white;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 1, 1);"
    );

    lblCategoria = new Label("Esperando...");
    lblCategoria.setStyle(
            "-fx-font-size: 16px;" +
                    "-fx-text-fill: rgba(255,255,255,0.9);"
    );

    turnoContainer.getChildren().addAll(lblTurnoActual, lblCategoria);

    // Estado del sistema
    lblEstado = new Label("Sistema Activo");
    lblEstado.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE, 14, Color.web("#27ae60")));
    lblEstado.setStyle(
            "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #27ae60;" +
                    "-fx-padding: 10px 20px;" +
                    "-fx-background-color: #d5f4e6;" +
                    "-fx-background-radius: 20px;"
    );

    // Título para la tabla
    Label tituloTabla = new Label("Próximos Turnos");
    tituloTabla.setStyle(
            "-fx-font-size: 15px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #2c3e50;" +
                    "-fx-padding: 10px 0 5px 0;"
    );

    // Usar la tabla estilizada
    tablaUnificada = createStyledTable();

    // Contenedor para la tabla con un poco de padding
    VBox tablaContainer = new VBox(5);
    tablaContainer.setPadding(new Insets(5, 0, 0, 0));
    tablaContainer.getChildren().addAll(tituloTabla, tablaUnificada);
    VBox.setVgrow(tablaContainer, Priority.ALWAYS);
    VBox.setVgrow(tablaUnificada, Priority.ALWAYS);

    panel.getChildren().addAll(titulo, turnoContainer, lblEstado, tablaContainer);

    return panel;
}





private void actualizarTablaUnificada() {
    new Thread(() -> {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // General
            URL urlGen = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/abiertos?preferente=false");
            Turno[] generales = mapper.readValue(urlGen, Turno[].class);

            // Preferencial
            URL urlPref = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/abiertos?preferente=true");
            Turno[] preferenciales = mapper.readValue(urlPref, Turno[].class);

            // Unir en filas
            ObservableList<TurnoDoble> filas = FXCollections.observableArrayList();
            int max = Math.max(generales.length, preferenciales.length);
            for (int i = 0; i < max; i++) {
                String gen = (i < generales.length) ?
                        generales[i].getId() + " - " + generales[i].getNumero() : "";
                String pref = (i < preferenciales.length) ?
                        preferenciales[i].getId() + " - " + preferenciales[i].getNumero() : "";

                filas.add(new TurnoDoble(gen, pref));
            }

            Platform.runLater(() -> tablaUnificada.setItems(filas));
        } catch (Exception e) {
            Platform.runLater(() -> {
                tablaUnificada.setPlaceholder(new Label("Error al cargar los datos"));
            });
            e.printStackTrace();
        }
    }).start();
}




    private VBox createStatsPanel() {
        VBox panel = new VBox(20);
        panel.setMinWidth(250);
        panel.setPadding(new Insets(25));
        panel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );

        // Título del panel
        Label titulo = new Label("TURNOS DEL DÍA");
        titulo.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLIPBOARD_LIST, 18, Color.web("#2c3e50")));
        titulo.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;"
        );

        // Separador
        Separator separador = new Separator();
        separador.setStyle("-fx-background-color: #ecf0f1;");

        // Contenedor de estadísticas
        statsContainer = new VBox(15);


        ScrollPane scrollStats = new ScrollPane(statsContainer);
        scrollStats.setFitToWidth(true);
        scrollStats.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollStats, Priority.ALWAYS);

        panel.getChildren().addAll(titulo, separador, scrollStats);

        return panel;
    }

    private VBox createStatCard(String icon, String titulo, String valor, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 0 0 0 4px;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        org.kordamp.ikonli.javafx.FontIcon iconLabel = new org.kordamp.ikonli.javafx.FontIcon();
        iconLabel.setIconLiteral(icon);
        iconLabel.setIconSize(20);
        iconLabel.setIconColor(Color.web(color));

        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #7f8c8d;" +
                        "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(iconLabel, tituloLabel);

        VBox content = new VBox(5);
        Label valorLabel = new Label(valor);
        valorLabel.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + color + ";"
        );

        content.getChildren().add(valorLabel);

        card.getChildren().addAll(header, content);

        return card;
    }

    // MÉTODO CORREGIDO PARA OBTENER EL LABEL DE VALOR
    private Label getStatValueLabel(VBox statCard) {
        // La estructura es: VBox -> [HBox (header), VBox (content)]
        // En content: VBox -> [Label (valor)]
        VBox contentContainer = (VBox) statCard.getChildren().get(1);
        return (Label) contentContainer.getChildren().get(0);
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(15, 30, 15, 30));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle(
                "-fx-background-color: #34495e;" +
                        "-fx-border-color: #2c3e50;" +
                        "-fx-border-width: 1px 0 0 0;"
        );

        Label info = new Label("Sistema de Turnos v2.0 | Desarrollado por kubocode | 2025");
        info.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.DESKTOP, 12, Color.web("#bdc3c7")));
        info.setWrapText(true);
        info.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #bdc3c7;"
        );

        footer.getChildren().add(info);

        return footer;
    }

    private Button createStyledButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(45);
        button.setStyle(
                "-fx-background-color: " + baseColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );

        // Efectos hover
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + hoverColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + baseColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }

    private void inicializarSistema() {
        actualizarTurnoActual();
        actualizarEstadisticas();
        cargarCategoriasDesdeAPI();

        // Animación de entrada
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> {
            playEntryAnimation();
        }));
        timeline.play();
    }

    private void configurarActualizacionesAutomaticas() {
        scheduler = Executors.newScheduledThreadPool(2);

        // Actualizar turno actual cada 3 segundos
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::actualizarTurnoActual);
        }, 3, 3, TimeUnit.SECONDS);

        // Actualizar hora cada segundo
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::actualizarHora);
        }, 1, 1, TimeUnit.SECONDS);

        // Actualizar estadísticas cada 10 segundos
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::actualizarEstadisticas);
        }, 10, 10, TimeUnit.SECONDS);
        //Actualizar Tablas de General y Preferencial
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::actualizarTablaUnificada);
        }, 5, 5, TimeUnit.SECONDS);

    }

    private void actualizarHora() {
        LocalDateTime now = LocalDateTime.now();
        String horaFormateada = now.format(DateTimeFormatter.ofPattern("HH:mm:ss | dd/MM/yyyy"));
        lblHora.setText(horaFormateada);
        lblHora.setGraphic(FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CLOCK, 14, Color.web("rgba(255,255,255,0.8)")));
    }

    private void actualizarTurnoActual() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/ultimo");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    InputStream input = conn.getInputStream();
                    ObjectMapper mapper = new ObjectMapper();
                    Turno turno = mapper.readValue(input, Turno.class);

                    Platform.runLater(() -> {
                        idTurnoActual = turno.getId();
                        lblTurnoActual.setText(String.valueOf(turno.getNumero()));
                        Categoria cat = turno.getCategoria();
                        String nombreCat = (cat != null) ? cat.getNombre() : "-";
                        lblCategoria.setText("Categoría: " + nombreCat);
                        playUpdateAnimation(lblTurnoActual);
                    });
                } else {
                    Platform.runLater(() -> {
                        lblTurnoActual.setText("---");
                        lblCategoria.setText("Sin turnos llamados");
                    });
                }
            } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
                Platform.runLater(() -> {
                    lblTurnoActual.setText("---");
                    lblCategoria.setText("Servidor no disponible");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblTurnoActual.setText("---");
                    lblCategoria.setText("Sin turnos llamados");
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void llamarSiguienteTurno() {
        showLoading(true);

        Timeline delay = new Timeline(new KeyFrame(Duration.millis(800), e -> {
            avanzarTurno(comboPuestos.getValue());
            showLoading(false);
        }));
        delay.play();
    }

    private void reLlamarTurnoActual() {
        if (idTurnoActual == null) {
            mostrarAlerta("Sin turno", "No hay ningún turno actual para re-llamar.");
            return;
        }

        final Long idParaRellamar = idTurnoActual;

        new Thread(() -> {
            try {
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/" + idParaRellamar + "/rellamar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                final int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode == 200) {
                    Platform.runLater(this::playSuccessAnimation);
                } else {
                    Platform.runLater(() -> mostrarAlerta("Error", "No se pudo re-llamar al turno. Código: " + responseCode));
                }
            } catch (Exception e) {
                Platform.runLater(() -> mostrarAlerta("Error", "Error de conexión al servidor: " + e.getMessage()));
            }
        }).start();
    }

private void avanzarTurno(int puestoSeleccionado) {
    // Recolectar los datos en el hilo de FX antes de lanzar el hilo de red
    int usuarioId = UsuarioLogeado.obtenerUsuario().getId();
    Categoria categoriaSeleccionada = comboCategorias.getValue();
    boolean preferente = comboTipo.getValue().equals("Preferencial");

    if (categoriaSeleccionada == null) {
        mostrarAlerta("Error", "Debe seleccionar una categoría.");
        return;
    }

    new Thread(() -> {
        HttpURLConnection conn = null;
        try {
            // Construir el JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("categoriaId", categoriaSeleccionada.getId());
            jsonMap.put("preferente", preferente);
            jsonMap.put("usuarioId", usuarioId);
            jsonMap.put("puesto", puestoSeleccionado);

            // Establecer conexión
            URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/cerrar");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Enviar el JSON
            mapper.writeValue(conn.getOutputStream(), jsonMap);

            // Manejar la respuesta
            if (conn.getResponseCode() == 200) {
                Platform.runLater(() -> {
                    actualizarTurnoActual();
                    actualizarEstadisticas();
                    actualizarTablaUnificada();
                    playSuccessAnimation();
                });
            } else {
                Platform.runLater(() -> {
                    mostrarAlerta("Sin Turnos", "No hay más turnos disponibles para procesar.");
                    lblTurnoActual.setText("---");
                    lblCategoria.setText("Sin turnos disponibles");
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                lblTurnoActual.setText("---");
                lblCategoria.setText("Error al cerrar turno");
                mostrarAlerta("Error", "Error de conexión al servidor: " + e.getMessage());
            });
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }).start();
}



    private void reiniciarTurnos() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Reinicio");
        confirmacion.setHeaderText("¿Está seguro de reiniciar todos los turnos?");
        confirmacion.setContentText("Esta acción implica que todos los contadores de turnos volverán a cero para iniciar una nueva jornada. Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/reiniciar");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    if (conn.getResponseCode() == 200 || conn.getResponseCode() == 204) {
                        Platform.runLater(() -> {
                            actualizarEstadisticas();
                            actualizarTurnoActual();
                            actualizarTablaUnificada();
                            mostrarAlerta("Reinicio Completo", "Los turnos han sido reiniciados exitosamente a cero.");
                        });
                    } else {
                        int code = conn.getResponseCode();
                        Platform.runLater(() -> {
                            mostrarAlerta("Error al Reiniciar", "El servidor respondió con código: " + code);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        mostrarAlerta("Error de Conexión", "No se pudo conectar al servidor: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

private void actualizarEstadisticas() {
    try {
        URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/conteo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() == 200) {
            InputStream input = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();

            // Parsear el JSON como Map<String, Integer>
            Map<String, Integer> conteoPorCategoria = mapper.readValue(input, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {});

            Platform.runLater(() -> {
                statsContainer.getChildren().clear(); // Limpiar anteriores

                for (Map.Entry<String, Integer> entry : conteoPorCategoria.entrySet()) {
                    String categoria = entry.getKey();
                    String cantidad = String.valueOf(entry.getValue());

                    // Ícono y color fijos
                    String icono = "fas-clipboard-check";
                    String color = "#6e0dc9";

                    VBox stat = createStatCard(icono, categoria, cantidad, color);
                    statsContainer.getChildren().add(stat);
                }

                playStatsUpdateAnimation();
            });

        } else {
            Platform.runLater(() -> {
                statsContainer.getChildren().clear();
                statsContainer.getChildren().add(new Label("Error al cargar estadísticas."));
            });
        }
    } catch (Exception e) {
        Platform.runLater(() -> {
            statsContainer.getChildren().clear();
            statsContainer.getChildren().add(new Label("Error de conexión al servidor."));
        });
    }
}



    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        btnLlamarTurno.setDisable(show);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Animaciones
    private void playEntryAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void playUpdateAnimation(Label label) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), label);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void playSuccessAnimation() {
        // Animación de pulso verde en el turno actual
        Timeline pulse = new Timeline();
        pulse.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(lblTurnoActual.scaleXProperty(), 1.0)),
                new KeyFrame(Duration.millis(150), new KeyValue(lblTurnoActual.scaleXProperty(), 1.15)),
                new KeyFrame(Duration.millis(300), new KeyValue(lblTurnoActual.scaleXProperty(), 1.0))
        );
        pulse.play();
    }

    private void playStateChangeAnimation() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), lblEstado);
        rotate.setByAngle(360);
        rotate.play();
    }

    private void playStatsUpdateAnimation() {
        for (javafx.scene.Node node : statsContainer.getChildren()) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), node);
            fade.setFromValue(0.7);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}