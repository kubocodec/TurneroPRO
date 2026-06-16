package com.turnero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnero.dto.MetricasGeneralesDTO;
import com.turnero.dto.UsuarioMetricaDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VistaMetricas {

    private DatePicker dpInicio;
    private DatePicker dpFin;
    private TableView<UsuarioMetricaDTO> tabla;
    private Label lblTotalDelDia;
    private Label lblAtendidos;
    private Label lblPendientes;
    private Label lblTiempoEspera;
    private Label lblTiempoAtencion;

    public VBox construir() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Título
        Label titulo = new Label("Métricas de Rendimiento");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Filtros
        HBox filtros = new HBox(15);
        filtros.setAlignment(Pos.CENTER_LEFT);
        dpInicio = new DatePicker(LocalDate.now());
        dpFin = new DatePicker(LocalDate.now());
        Button btnFiltrar = new Button("Actualizar Métricas");
        btnFiltrar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btnFiltrar.setOnAction(e -> cargarMetricas());
        filtros.getChildren().addAll(new Label("Desde:"), dpInicio, new Label("Hasta:"), dpFin, btnFiltrar);

        // Tarjetas Generales
        HBox cards = new HBox(15);
        lblTotalDelDia = createValueLabel();
        lblAtendidos = createValueLabel();
        lblPendientes = createValueLabel();
        lblTiempoEspera = createValueLabel();
        lblTiempoAtencion = createValueLabel();

        cards.getChildren().addAll(
                createCard("Total Turnos", lblTotalDelDia, "#3498db"),
                createCard("Atendidos", lblAtendidos, "#2ecc71"),
                createCard("Pendientes", lblPendientes, "#e74c3c"),
                createCard("Espera Promedio", lblTiempoEspera, "#f39c12"),
                createCard("Atención Promedio", lblTiempoAtencion, "#9b59b6")
        );

        // Tabla de Operadores
        tabla = new TableView<>();
        tabla.setPrefHeight(400);
        
        TableColumn<UsuarioMetricaDTO, String> colOperador = new TableColumn<>("Operador");
        colOperador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreUsuario()));
        
        TableColumn<UsuarioMetricaDTO, String> colTurnos = new TableColumn<>("Turnos Atendidos");
        colTurnos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTurnosAtendidos())));
        
        TableColumn<UsuarioMetricaDTO, String> colEspera = new TableColumn<>("Espera Promedio");
        colEspera.setCellValueFactory(d -> new SimpleStringProperty(formatearSegundos(d.getValue().getTiempoPromedioEsperaSegundos())));
        
        TableColumn<UsuarioMetricaDTO, String> colAtencion = new TableColumn<>("Atención Promedio");
        colAtencion.setCellValueFactory(d -> new SimpleStringProperty(formatearSegundos(d.getValue().getTiempoPromedioAtencionSegundos())));
        
        TableColumn<UsuarioMetricaDTO, String> colCalif = new TableColumn<>("Calificación Prom.");
        colCalif.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCalificacionPromedio()));
        
        tabla.getColumns().addAll(colOperador, colTurnos, colEspera, colAtencion, colCalif);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabla.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) {
                mostrarDetallesOperador(tabla.getSelectionModel().getSelectedItem());
            }
        });

        root.getChildren().addAll(titulo, filtros, cards, new Label("Métricas por Operador (Doble clic para ver detalles)"), tabla);
        
        cargarMetricas();
        
        return root;
    }

    private String formatearSegundos(double secs) {
        long m = (long) (secs / 60);
        long s = (long) (secs % 60);
        return String.format("%02d:%02d", m, s);
    }

    private Label createValueLabel() {
        Label lbl = new Label("-");
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        return lbl;
    }

    private VBox createCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-width: 0 0 0 4px; -fx-border-color: " + color + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        valueLabel.setTextFill(Color.web(color));
        card.getChildren().addAll(lblTitle, valueLabel);
        return card;
    }

    private void cargarMetricas() {
        new Thread(() -> {
            try {
                LocalDateTime start = dpInicio.getValue().atStartOfDay();
                LocalDateTime end = dpFin.getValue().atTime(23, 59, 59);
                
                String startStr = start.toString();
                String endStr = end.toString();
                
                URL url = new URL("http://" + ConfigManager.getIp() + ":8080/api/turnos/metricas/general?start=" + startStr + "&end=" + endStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    MetricasGeneralesDTO data = mapper.readValue(conn.getInputStream(), MetricasGeneralesDTO.class);
                    
                    Platform.runLater(() -> {
                        lblTotalDelDia.setText(String.valueOf(data.getTotalTurnosDelDia()));
                        lblAtendidos.setText(String.valueOf(data.getTurnosAtendidos()));
                        lblPendientes.setText(String.valueOf(data.getTurnosPendientes()));
                        lblTiempoEspera.setText(formatearSegundos(data.getTiempoPromedioEsperaGeneral()));
                        lblTiempoAtencion.setText(formatearSegundos(data.getTiempoPromedioAtencionGeneral()));
                        
                        if(data.getMetricasPorUsuario() != null) {
                            tabla.setItems(FXCollections.observableArrayList(data.getMetricasPorUsuario()));
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarDetallesOperador(UsuarioMetricaDTO op) {
        new Thread(() -> {
            try {
                LocalDateTime start = dpInicio.getValue().atStartOfDay();
                LocalDateTime end = dpFin.getValue().atTime(23, 59, 59);
                
                String startStr = start.toString();
                String endStr = end.toString();
                
                URL url = new URL("http://" + com.turnero.ConfigManager.getIp() + ":8080/api/turnos/metricas/usuario/" + op.getUsuarioId() + "?start=" + startStr + "&end=" + endStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    com.turnero.dto.MetricasUsuarioDTO data = mapper.readValue(conn.getInputStream(), com.turnero.dto.MetricasUsuarioDTO.class);
                    
                    Platform.runLater(() -> {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("Detalles de Turnos - " + op.getNombreUsuario());
                        dialog.setHeaderText("Desglose de turnos atendidos por " + op.getNombreUsuario());
                        
                        TableView<com.turnero.dto.TurnoDetalleDTO> tablaDetalles = new TableView<>();
                        tablaDetalles.setPrefSize(700, 400);
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colNum = new TableColumn<>("Número");
                        colNum.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumero()));
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colCat = new TableColumn<>("Categoría");
                        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colEspera = new TableColumn<>("Espera");
                        colEspera.setCellValueFactory(d -> new SimpleStringProperty(formatearSegundos(d.getValue().getTiempoEsperaSegundos())));
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colAtencion = new TableColumn<>("Atención");
                        colAtencion.setCellValueFactory(d -> new SimpleStringProperty(formatearSegundos(d.getValue().getTiempoAtencionSegundos())));
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colCalif = new TableColumn<>("Calificación");
                        colCalif.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCalificacion()));
                        
                        TableColumn<com.turnero.dto.TurnoDetalleDTO, String> colObs = new TableColumn<>("Observaciones");
                        colObs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservaciones()));
                        
                        tablaDetalles.getColumns().addAll(colNum, colCat, colEspera, colAtencion, colCalif, colObs);
                        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                        
                        if(data.getDetalleTurnos() != null) {
                            tablaDetalles.setItems(FXCollections.observableArrayList(data.getDetalleTurnos()));
                        }
                        
                        dialog.getDialogPane().setContent(tablaDetalles);
                        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                        dialog.showAndWait();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
