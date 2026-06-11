package com.kubocode.turnero;

import com.kubocode.turnero.license.LicenseDialog;
import com.kubocode.turnero.license.LicenseManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TrayLauncher {

    public static void main(String[] args) {
        // Establecer el Look and Feel local de Windows
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1. Verificación de Licencia
        if (!LicenseManager.isLicenseValid()) {
            LicenseDialog dialog = new LicenseDialog();
            dialog.setVisible(true);

            // Si se cerró el diálogo y no se activó, terminar la app
            if (!dialog.isActivated() && !LicenseManager.isLicenseValid()) {
                System.out.println("No hay licencia válida. Saliendo...");
                System.exit(0);
            }
        }

        // 2. Si la licencia es válida, arrancar el servidor en un hilo secundario
        new Thread(() -> {
            try {
                TurneroApplication.main(args);
                System.out.println("Servidor turnero arrancado con éxito.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Lanzar la pantalla cliente automáticamente
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Dar 3 segundos para que arranque el servidor Spring
                abrirPantallaCliente();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 3. Inicializar el System Tray
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = null;

        try {
            URL iconURL = TrayLauncher.class.getResource("/turno.ico");
            if (iconURL != null) {
                // Java's ImageIO doesn't natively support .ico, so it might return null
                image = ImageIO.read(iconURL);
                if (image == null) {
                    // Try with Toolkit
                    image = Toolkit.getDefaultToolkit().getImage(iconURL);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (image == null) {
            System.out.println("Usando ícono genérico.");
            image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(new Color(41, 128, 185));
            g.fillRect(0, 0, 16, 16);
            g.dispose();
        }

        PopupMenu popup = new PopupMenu();

        // Opción: Abrir Pantalla Cliente
        MenuItem openClientItem = new MenuItem("Abrir Pantalla Cliente");
        openClientItem.addActionListener(e -> abrirPantallaCliente());
        popup.add(openClientItem);

        popup.addSeparator();

        // Opción: Salir
        MenuItem exitItem = new MenuItem("Cerrar Servidor");
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "¿Estás seguro de que deseas cerrar el servidor de turnos?\nEsto desconectará todas las pantallas.",
                    "Cerrar Sistema Turnero", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "Servidor Turnero", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            trayIcon.displayMessage("Sistema Turnero", "El servidor se está ejecutando en segundo plano.", TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private static void abrirPantallaCliente() {
        try {
            // Ruta relativa asumiendo que el exe de la pantalla está dentro de la carpeta PantallaTurnero
            File pantallaExe = new File("PantallaTurnero", "PantallaTurnero.exe");
            
            if (pantallaExe.exists()) {
                new ProcessBuilder(pantallaExe.getAbsolutePath()).start();
            } else {
                JOptionPane.showMessageDialog(null, 
                    "No se encontró el ejecutable de la Pantalla Cliente.\n" +
                    "Ruta buscada: " + pantallaExe.getAbsolutePath(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al abrir la Pantalla Cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
