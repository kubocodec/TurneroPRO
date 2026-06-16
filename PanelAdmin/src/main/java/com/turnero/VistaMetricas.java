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
import javafx.scene.chart.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class VistaMetricas {

    private DatePicker dpInicio;
    private DatePicker dpFin;
    private TableView<UsuarioMetricaDTO> tabla;
    private Label lblTotalDelDia;
    private Label lblAtendidos;
    private Label lblPendientes;
    private Label lblTiempoEspera;
    private Label lblTiempoAtencion;

    private PieChart chartCalificaciones;
    private BarChart<String, Number> chartOperadores;
    private MetricasGeneralesDTO datosActuales;

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
        
        Button btnExportar = new Button("Exportar a PDF");
        btnExportar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExportar.setOnAction(e -> exportarPDF());

        filtros.getChildren().addAll(new Label("Desde:"), dpInicio, new Label("Hasta:"), dpFin, btnFiltrar, btnExportar);

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

        // Gráficos
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        
        chartCalificaciones = new PieChart();
        chartCalificaciones.setTitle("Calificaciones Globales");
        chartCalificaciones.setPrefSize(400, 300);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Operador");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Turnos Atendidos");
        chartOperadores = new BarChart<>(xAxis, yAxis);
        chartOperadores.setTitle("Rendimiento por Operador");
        chartOperadores.setPrefSize(500, 300);
        chartOperadores.setLegendVisible(false);

        chartsBox.getChildren().addAll(chartCalificaciones, chartOperadores);

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

        root.getChildren().addAll(titulo, filtros, cards, chartsBox, new Label("Métricas por Operador (Doble clic para ver detalles)"), tabla);
        
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
                    datosActuales = data;
                    Platform.runLater(() -> {
                        lblTotalDelDia.setText(String.valueOf(data.getTotalTurnosDelDia()));
                        lblAtendidos.setText(String.valueOf(data.getTurnosAtendidos()));
                        lblPendientes.setText(String.valueOf(data.getTurnosPendientes()));
                        lblTiempoEspera.setText(formatearSegundos(data.getTiempoPromedioEsperaGeneral()));
                        lblTiempoAtencion.setText(formatearSegundos(data.getTiempoPromedioAtencionGeneral()));
                        
                        if(data.getMetricasPorUsuario() != null) {
                            tabla.setItems(FXCollections.observableArrayList(data.getMetricasPorUsuario()));
                            
                            XYChart.Series<String, Number> series = new XYChart.Series<>();
                            for (UsuarioMetricaDTO u : data.getMetricasPorUsuario()) {
                                series.getData().add(new XYChart.Data<>(u.getNombreUsuario(), u.getTurnosAtendidos()));
                            }
                            chartOperadores.getData().clear();
                            chartOperadores.getData().add(series);
                        }

                        if (data.getDistribucionCalificaciones() != null) {
                            chartCalificaciones.getData().clear();
                            for (Map.Entry<String, Long> entry : data.getDistribucionCalificaciones().entrySet()) {
                                chartCalificaciones.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                            }
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

    private void exportarPDF() {
        if (datosActuales == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No hay datos para exportar. Actualice las métricas primero.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Reporte_Turnero_" + LocalDate.now() + ".pdf");
        
        File file = fileChooser.showSaveDialog(dpInicio.getScene().getWindow());
        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Fuentes
                Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, java.awt.Color.DARK_GRAY);
                Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.GRAY);
                Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);
                Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);

                // Título
                Paragraph titulo = new Paragraph("REPORTE OFICIAL DE ATENCIÓN - TURNERO PRO", fontTitulo);
                titulo.setAlignment(Element.ALIGN_CENTER);
                titulo.setSpacingAfter(20);
                document.add(titulo);

                Paragraph periodo = new Paragraph("Periodo de evaluación: " + dpInicio.getValue() + " a " + dpFin.getValue(), fontNormal);
                periodo.setAlignment(Element.ALIGN_CENTER);
                periodo.setSpacingAfter(30);
                document.add(periodo);

                // Resumen Ejecutivo
                document.add(new Paragraph("RESUMEN EJECUTIVO", fontSubtitulo));
                document.add(new Paragraph(" "));
                
                PdfPTable tableResumen = new PdfPTable(2);
                tableResumen.setWidthPercentage(80);
                tableResumen.setHorizontalAlignment(Element.ALIGN_CENTER);
                
                tableResumen.addCell(new Phrase("Total Turnos Emitidos:", fontNormal));
                tableResumen.addCell(new Phrase(String.valueOf(datosActuales.getTotalTurnosDelDia()), fontNormal));
                tableResumen.addCell(new Phrase("Turnos Atendidos:", fontNormal));
                tableResumen.addCell(new Phrase(String.valueOf(datosActuales.getTurnosAtendidos()), fontNormal));
                tableResumen.addCell(new Phrase("Turnos Pendientes/Abandonados:", fontNormal));
                tableResumen.addCell(new Phrase(String.valueOf(datosActuales.getTurnosPendientes()), fontNormal));
                tableResumen.addCell(new Phrase("Tiempo Espera Promedio:", fontNormal));
                tableResumen.addCell(new Phrase(formatearSegundos(datosActuales.getTiempoPromedioEsperaGeneral()), fontNormal));
                tableResumen.addCell(new Phrase("Tiempo Atención Promedio:", fontNormal));
                tableResumen.addCell(new Phrase(formatearSegundos(datosActuales.getTiempoPromedioAtencionGeneral()), fontNormal));
                
                document.add(tableResumen);
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));

                // Tabla de Operadores
                document.add(new Paragraph("RENDIMIENTO POR OPERADOR", fontSubtitulo));
                document.add(new Paragraph(" "));

                PdfPTable tableOps = new PdfPTable(5);
                tableOps.setWidthPercentage(100);
                
                String[] headers = {"Operador", "Atendidos", "Espera Prom.", "Atención Prom.", "Calificación Prom."};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(new java.awt.Color(41, 128, 185)); // Azul corporativo
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(8);
                    tableOps.addCell(cell);
                }

                if (datosActuales.getMetricasPorUsuario() != null) {
                    for (UsuarioMetricaDTO u : datosActuales.getMetricasPorUsuario()) {
                        PdfPCell c1 = new PdfPCell(new Phrase(u.getNombreUsuario(), fontNormal));
                        PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(u.getTurnosAtendidos()), fontNormal));
                        PdfPCell c3 = new PdfPCell(new Phrase(formatearSegundos(u.getTiempoPromedioEsperaSegundos()), fontNormal));
                        PdfPCell c4 = new PdfPCell(new Phrase(formatearSegundos(u.getTiempoPromedioAtencionSegundos()), fontNormal));
                        PdfPCell c5 = new PdfPCell(new Phrase(u.getCalificacionPromedio(), fontNormal));
                        
                        c1.setPadding(6); c2.setPadding(6); c3.setPadding(6); c4.setPadding(6); c5.setPadding(6);
                        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c4.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c5.setHorizontalAlignment(Element.ALIGN_CENTER);
                        
                        tableOps.addCell(c1); tableOps.addCell(c2); tableOps.addCell(c3); tableOps.addCell(c4); tableOps.addCell(c5);
                    }
                }
                document.add(tableOps);

                document.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Reporte PDF exportado exitosamente en:\n" + file.getAbsolutePath(), ButtonType.OK);
                alert.showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar el archivo PDF: " + ex.getMessage(), ButtonType.OK);
                alert.showAndWait();
                ex.printStackTrace();
            }
        }
    }
}
