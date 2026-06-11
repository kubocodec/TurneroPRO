package com.turnero;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LoginView extends Application {

    private Stage primaryStage;
    private VBox loginContainer;
    private Label errorLabel;
    private TextField txtUsuario;
    private PasswordField txtPassword;
    private TextField txtPasswordVisible; // Campo de texto visible para mostrar contraseña
    private Button btnTogglePassword; // Botón del ojo
    private Button btnLogin;
    private ProgressIndicator loadingIndicator;
    private boolean isPasswordVisible = false; // Estado del ojo

    @Override
    public void start(Stage stage) {
        if (ConfigManager.getIp() == null) {
            ConfigWindow cw = new ConfigWindow();
            cw.start(new Stage());
            return;
        }
        this.primaryStage = stage;

        // Configurar ventana sin decoraciones
        stage.initStyle(StageStyle.UNDECORATED);

        // Crear el layout principal
        StackPane root = createMainLayout();

        // Crear la escena
        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.setTitle("Sistema de Administración - Login");
        stage.centerOnScreen();

        // Animación de entrada
        playEntryAnimation();

        stage.show();
    }

    private StackPane createMainLayout() {
        StackPane root = new StackPane();

        // Fondo con gradiente
        Rectangle background = createGradientBackground();

        // Contenedor principal con efecto blur de fondo
        HBox mainContainer = new HBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(0);

        // Panel izquierdo - Información de la empresa
        VBox leftPanel = createLeftPanel();

        // Panel derecho - Formulario de login
        VBox rightPanel = createRightPanel();

        mainContainer.getChildren().addAll(leftPanel, rightPanel);

        // Aplicar efecto de sombra al contenedor principal
        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setOffsetX(0);
        shadow.setOffsetY(10);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        mainContainer.setEffect(shadow);

        root.getChildren().addAll(background, mainContainer);

        return root;
    }

    private Rectangle createGradientBackground() {
        Rectangle background = new Rectangle(1000, 700);

        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, null,
                new Stop(0, Color.rgb(41, 128, 185)),
                new Stop(0.5, Color.rgb(52, 152, 219)),
                new Stop(1, Color.rgb(155, 207, 255))
        );

        background.setFill(gradient);
        return background;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(500);
        leftPanel.setPrefHeight(500);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setSpacing(30);
        leftPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2980b9, #3498db);" +
                        "-fx-background-radius: 15 0 0 15;"
        );

        // Logo/Icono de la empresa
        Label logoIcon = new Label("🏢");
        logoIcon.setStyle(
                "-fx-font-size: 80px;" +
                        "-fx-text-fill: white;"
        );

        // Título de bienvenida
        Label welcomeTitle = new Label("Bienvenido al");
        welcomeTitle.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label systemTitle = new Label("SISTEMA DE ADMINISTRACIÓN");
        systemTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: rgba(255, 255, 255, 0.9);" +
                        "-fx-text-alignment: center;"
        );

        // Descripción
        Label description = new Label("Gestiona tu negocio de manera\nprofesional y eficiente");
        description.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: rgba(255, 255, 255, 0.8);" +
                        "-fx-text-alignment: center;"
        );

        // Elementos decorativos
        HBox decorativeElements = new HBox(20);
        decorativeElements.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            Rectangle dot = new Rectangle(12, 12);
            dot.setFill(Color.rgb(255, 255, 255, 0.6));
            dot.setArcWidth(12);
            dot.setArcHeight(12);
            decorativeElements.getChildren().add(dot);
        }

        leftPanel.getChildren().addAll(
                logoIcon, welcomeTitle, systemTitle, description, decorativeElements
        );

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.setPrefWidth(500);
        rightPanel.setPrefHeight(500);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setSpacing(25);
        rightPanel.setPadding(new Insets(60));
        rightPanel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 0 15 15 0;"
        );

        // Título del formulario
        Label formTitle = new Label("Iniciar Sesión");
        formTitle.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;"
        );

        Label formSubtitle = new Label("Ingresa tus credenciales para continuar");
        formSubtitle.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #7f8c8d;"
        );

        // Contenedor del formulario
        loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);

        // Campo de usuario
        VBox userFieldContainer = createStyledField("👤 Usuario", true);
        txtUsuario = (TextField) userFieldContainer.getChildren().get(1);

        // Campo de contraseña CON OJO
        VBox passwordFieldContainer = createPasswordFieldWithToggle();

        // Botón de login
        btnLogin = createStyledButton();

        // Indicador de carga
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(30, 30);
        loadingIndicator.setVisible(false);

        // Label de error
        errorLabel = new Label();
        errorLabel.setStyle(
                "-fx-text-fill: #e74c3c;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        errorLabel.setVisible(false);

        // Configurar eventos
        setupEventHandlers();

        loginContainer.getChildren().addAll(
                userFieldContainer,
                passwordFieldContainer,
                btnLogin,
                loadingIndicator,
                errorLabel
        );

        // Botón para cambiar IP del Servidor
        Button btnConfigServer = new Button("Cambiar IP Servidor");
        btnConfigServer.setGraphic(org.kordamp.ikonli.javafx.FontIcon.of(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.COG, 14, javafx.scene.paint.Color.web("#7f8c8d")));
        btnConfigServer.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;");
        btnConfigServer.setOnAction(e -> {
            ConfigWindow cw = new ConfigWindow();
            cw.start(new Stage());
            primaryStage.close();
        });
        
        VBox footer = new VBox(btnConfigServer);
        footer.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setMargin(footer, new Insets(30, 0, 0, 0));

        rightPanel.getChildren().addAll(
                formTitle,
                formSubtitle,
                loginContainer,
                footer
        );

        return rightPanel;
    }

    // NUEVO MÉTODO PARA CREAR EL CAMPO DE CONTRASEÑA CON OJO
    private VBox createPasswordFieldWithToggle() {
        VBox container = new VBox(8);

        Label label = new Label("🔒 Contraseña");
        label.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #34495e;"
        );

        // Contenedor para el campo y el botón del ojo
        StackPane fieldContainer = new StackPane();
        fieldContainer.setPrefHeight(45);

        // Campo de contraseña (oculto)
        txtPassword = new PasswordField();
        txtPassword.setPrefHeight(45);
        applyFieldStyle(txtPassword);

        // Campo de texto visible (inicialmente oculto)
        txtPasswordVisible = new TextField();
        txtPasswordVisible.setPrefHeight(45);
        txtPasswordVisible.setVisible(false);
        applyFieldStyle(txtPasswordVisible);

        // Botón del ojo
        btnTogglePassword = new Button("👁");
        btnTogglePassword.setPrefSize(35, 35);
        btnTogglePassword.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 16px;" +
                        "-fx-cursor: hand;" +
                        "-fx-text-fill: #7f8c8d;"
        );

        // Posicionar el botón del ojo a la derecha
        StackPane.setAlignment(btnTogglePassword, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnTogglePassword, new Insets(0, 10, 0, 0));

        // Evento del botón del ojo
        btnTogglePassword.setOnAction(e -> togglePasswordVisibility());

        // Efectos hover para el botón del ojo
        btnTogglePassword.setOnMouseEntered(e -> {
            btnTogglePassword.setStyle(
                    "-fx-background-color: rgba(52, 152, 219, 0.1);" +
                            "-fx-border-color: transparent;" +
                            "-fx-background-radius: 50%;" +
                            "-fx-font-size: 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-text-fill: #3498db;"
            );
        });

        btnTogglePassword.setOnMouseExited(e -> {
            btnTogglePassword.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;" +
                            "-fx-font-size: 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-text-fill: #7f8c8d;"
            );
        });

        fieldContainer.getChildren().addAll(txtPassword, txtPasswordVisible, btnTogglePassword);
        container.getChildren().addAll(label, fieldContainer);

        return container;
    }

    // MÉTODO PARA ALTERNAR VISIBILIDAD DE CONTRASEÑA
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            txtPasswordVisible.setVisible(false);
            txtPassword.setVisible(true);
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getText().length());
            btnTogglePassword.setText("👁");
            isPasswordVisible = false;
        } else {
            // Mostrar contraseña
            txtPassword.setVisible(false);
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
            btnTogglePassword.setText("🙈");
            isPasswordVisible = true;
        }
    }

    // MÉTODO PARA APLICAR ESTILOS A LOS CAMPOS
    private void applyFieldStyle(Control field) {
        field.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 50px 0 15px;" + // Padding derecho para el botón del ojo
                        "-fx-text-fill: #2c3e50;"
        );

        // Efectos de focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #3498db;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 0 50px 0 15px;" +
                                "-fx-text-fill: #2c3e50;" +
                                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 10, 0, 0, 0);"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: #f8f9fa;" +
                                "-fx-border-color: #e9ecef;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 0 50px 0 15px;" +
                                "-fx-text-fill: #2c3e50;"
                );
            }
        });
    }

    private VBox createStyledField(String labelText, boolean isTextField) {
        VBox container = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #34495e;"
        );

        Control field;
        if (isTextField) {
            field = new TextField();
        } else {
            field = new PasswordField();
        }

        field.setPrefHeight(45);
        field.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 15px;" +
                        "-fx-text-fill: #2c3e50;"
        );

        // Efectos de focus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #3498db;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 0 15px;" +
                                "-fx-text-fill: #2c3e50;" +
                                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 10, 0, 0, 0);"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: #f8f9fa;" +
                                "-fx-border-color: #e9ecef;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 0 15px;" +
                                "-fx-text-fill: #2c3e50;"
                );
            }
        });

        container.getChildren().addAll(label, field);
        return container;
    }

    private Button createStyledButton() {
        Button button = new Button("INICIAR SESIÓN");
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        button.setStyle(
                "-fx-background-color: linear-gradient(to right, #3498db, #2980b9);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 25px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.4), 15, 0, 0, 5);"
        );

        // Efectos hover
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2980b9, #1f618d);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 25px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.6), 20, 0, 0, 8);"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3498db, #2980b9);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 25px;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.4), 15, 0, 0, 5);"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }

    private void setupEventHandlers() {
        btnLogin.setOnAction(e -> handleLogin());

        // Enter key para login - funciona con ambos campos de contraseña
        txtPassword.setOnAction(e -> handleLogin());
        txtPasswordVisible.setOnAction(e -> handleLogin());
        txtUsuario.setOnAction(e -> {
            if (isPasswordVisible) {
                txtPasswordVisible.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
        });
    }

    private void handleLogin() {
        String user = txtUsuario.getText().trim();
        String pass = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

        // Ocultar error anterior
        hideError();

        // Validaciones
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Por favor, complete todos los campos");
            return;
        }

        // Mostrar loading
        showLoading(true);

        // TIEMPO REDUCIDO DE 1.5 SEGUNDOS A 0.5 SEGUNDOS
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            showLoading(false);
            new Thread(() -> {
                try {
                    // Crear JSON de login
                    String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", user, pass);

                    URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/auth/login");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // Enviar JSON
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = json.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Leer respuesta
                    int status = conn.getResponseCode();
                    if (status == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line.trim());
                        }

                        // Parsear JSON
                        String jsonResponse = response.toString();
                        Usuario usuario = parsearUsuario(jsonResponse);
                        UsuarioLogeado.guardarUsuario(usuario);

                        Platform.runLater(() -> {
                            showLoading(false);
                            playSuccessAnimation(() -> {
                                PanelAdmin panel = new PanelAdmin();
                                try {
                                    panel.start(new Stage());
                                    primaryStage.close();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        });

                    } else {
                        Platform.runLater(() -> {
                            showLoading(false);
                            showError("Usuario o contraseña incorrectos.");
                            playShakeAnimation();
                        });
                    }

                    conn.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        showLoading(false);
                        showError("Error de conexión con el servidor.");
                        playShakeAnimation();
                    });
                }
            }).start();


        }));
        timeline.play();
    }

    private Usuario parsearUsuario(String json) {
        JSONObject obj = new JSONObject(json);
        Usuario u = new Usuario();
        u.setId(obj.getInt("id"));
        u.setNombre(obj.getString("nombre"));
        u.setUsername(obj.getString("username"));
        u.setPassword(obj.getString("password"));
        u.setRol(obj.getString("rol"));
        return u;
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        btnLogin.setDisable(show);

        if (show) {
            btnLogin.setText("VERIFICANDO...");
        } else {
            btnLogin.setText("INICIAR SESIÓN");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Animación de aparición del error
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void hideError() {
        if (errorLabel.isVisible()) {
            FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> errorLabel.setVisible(false));
            fade.play();
        }
    }

    private void playEntryAnimation() {
        // Animación de entrada desde abajo
        TranslateTransition translate = new TranslateTransition(Duration.millis(800), loginContainer);
        translate.setFromY(50);
        translate.setToY(0);

        FadeTransition fade = new FadeTransition(Duration.millis(800), loginContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.setInterpolator(Interpolator.EASE_OUT);
        parallel.play();
    }

    private void playShakeAnimation() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginContainer);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void playSuccessAnimation(Runnable onComplete) {
        // Animación de éxito con escala
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginContainer);
        scale.setToX(1.1);
        scale.setToY(1.1);

        ScaleTransition scaleBack = new ScaleTransition(Duration.millis(200), loginContainer);
        scaleBack.setToX(1.0);
        scaleBack.setToY(1.0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginContainer);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(scale, scaleBack, fadeOut);
        sequence.setOnFinished(e -> onComplete.run());
        sequence.play();
    }

    public static void main(String[] args) {
        if (ConfigManager.getIp() == null) {
            Application.launch(ConfigWindow.class, args);
        } else {
            launch(args);
        }
    }
}