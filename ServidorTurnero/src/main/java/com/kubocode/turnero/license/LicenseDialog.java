package com.kubocode.turnero.license;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LicenseDialog extends JDialog {

    private boolean isActivated = false;
    private JTextField txtLicenseKey;
    private JTextField txtHardwareId;

    public LicenseDialog() {
        super((Frame) null, "Activación del Sistema Turnero", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(null); // Center on screen
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // --- Cabecero ---
        JLabel lblTitulo = new JLabel("Sistema no activado");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.RED);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTextArea lblMensaje = new JTextArea(
            "Su copia de Sistema Turnero no está activada o es inválida.\n" +
            "Para activarlo, envíe el siguiente ID de Hardware al administrador\n" +
            "o desarrollador (KuboCode) para recibir su clave de licencia."
        );
        lblMensaje.setEditable(false);
        lblMensaje.setOpaque(false);
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.add(lblTitulo, BorderLayout.NORTH);
        panelTitulo.add(lblMensaje, BorderLayout.CENTER);
        contentPane.add(panelTitulo, BorderLayout.NORTH);

        // --- Centro ---
        JPanel panelCentro = new JPanel(new GridLayout(4, 1, 5, 5));
        
        panelCentro.add(new JLabel("ID de Hardware (Copie y envíelo):"));
        txtHardwareId = new JTextField(LicenseManager.getHardwareId());
        txtHardwareId.setEditable(false);
        txtHardwareId.setFont(new Font("Monospaced", Font.BOLD, 14));
        txtHardwareId.setBackground(new Color(240, 240, 240));
        txtHardwareId.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelCentro.add(txtHardwareId);
        
        panelCentro.add(new JLabel("Clave de Activación:"));
        txtLicenseKey = new JTextField();
        txtLicenseKey.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtLicenseKey.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelCentro.add(txtLicenseKey);

        contentPane.add(panelCentro, BorderLayout.CENTER);

        // --- Botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnActivar = new JButton("Activar Sistema");
        JButton btnSalir = new JButton("Salir");
        
        btnActivar.setBackground(new Color(41, 128, 185)); // Azul
        btnActivar.setForeground(Color.WHITE);
        btnActivar.setFocusPainted(false);
        btnActivar.setFont(new Font("Arial", Font.BOLD, 14));

        btnActivar.addActionListener(e -> checkActivation());
        btnSalir.addActionListener(e -> dispose());
        
        panelBotones.add(btnSalir);
        panelBotones.add(btnActivar);

        contentPane.add(panelBotones, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
    }

    private void checkActivation() {
        String enteredKey = txtLicenseKey.getText().trim();
        if (enteredKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor introduzca una clave de activación.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String hwid = LicenseManager.getHardwareId();
        String expectedKey = LicenseManager.generateExpectedLicenseCode(hwid);
        
        if (enteredKey.equalsIgnoreCase(expectedKey)) {
            try {
                LicenseManager.saveLicense(enteredKey.toUpperCase());
                isActivated = true;
                JOptionPane.showMessageDialog(this, "¡Sistema activado con éxito!\nPor favor, ejecute de nuevo.", "Activación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar la licencia: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "La clave ingresada no es válida para esta computadora.", "Clave Inválida", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isActivated() {
        return isActivated;
    }
}
