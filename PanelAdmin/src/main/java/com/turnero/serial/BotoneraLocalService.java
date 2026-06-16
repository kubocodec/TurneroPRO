package com.turnero.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.turnero.ConfigManager;
import com.turnero.PanelAdmin;
import javafx.application.Platform;

public class BotoneraLocalService {

    private static SerialPort serialPort;
    private static Thread pollingThread;
    private static volatile boolean running = false;
    private static PanelAdmin controller;

    public static void iniciar(PanelAdmin ctrl) {
        if (!ConfigManager.isBotoneraHabilitada()) {
            System.out.println("[*] Calificador físico local deshabilitado en la configuración.");
            return;
        }
        controller = ctrl;
        String puerto = ConfigManager.getBotoneraPuerto();
        System.out.println("==========================================");
        System.out.println("   INICIANDO BOTONERA LOCAL (COM)         ");
        System.out.println("==========================================");

        new Thread(() -> {
            try {
                serialPort = SerialPort.getCommPort(puerto);
                serialPort.setBaudRate(9600);
                serialPort.setNumDataBits(8);
                serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
                serialPort.setParity(SerialPort.NO_PARITY);
                serialPort.setDTR();
                serialPort.setRTS();

                if (!serialPort.openPort()) {
                    System.err.println("[-] No se pudo abrir el puerto local " + puerto + ". Verifique conexión.");
                    return;
                }

                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 500, 500);
                Thread.sleep(1000); // Pausa para reset DTR

                // Enviar secuencia de despertar
                System.out.println("[+] Enviando secuencia de despertar local...");
                byte[] wakeupSequence = {(byte) 0xAA, (byte) 0xFF, (byte) 0x41, (byte) 0x61, (byte) 0x45};
                for (byte b : wakeupSequence) {
                    serialPort.writeBytes(new byte[]{b}, 1);
                    Thread.sleep(100);
                }

                running = true;
                pollingThread = new Thread(BotoneraLocalService::pollingLoop);
                pollingThread.setDaemon(true);
                pollingThread.start();
                System.out.println("[+] Botonera local en puerto " + puerto + " escuchando...");

            } catch (Exception e) {
                System.err.println("[-] Error al inicializar calificador local: " + e.getMessage());
            }
        }).start();
    }

    private static void pollingLoop() {
        while (running && serialPort != null && serialPort.isOpen()) {
            try {
                byte[] rCommand = {'R'};
                serialPort.writeBytes(rCommand, 1);
                Thread.sleep(100);

                int bytesAvailable = serialPort.bytesAvailable();
                if (bytesAvailable > 0) {
                    byte[] readBuffer = new byte[bytesAvailable];
                    int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                    for (int i = 0; i < numRead; i++) {
                        char c = (char) readBuffer[i];
                        procesarBoton(c);
                    }
                }
                Thread.sleep(400);
            } catch (Exception e) {
                if (running) {
                    System.err.println("[-] Error en bucle serial local: " + e.getMessage());
                }
            }
        }
    }

    private static void procesarBoton(char boton) {
        String calificacion;
        switch (boton) {
            case '1': calificacion = "MALO"; break;
            case '2': calificacion = "REGULAR"; break;
            case '3': calificacion = "BUENO"; break;
            case '4': calificacion = "EXCELENTE"; break;
            default: return;
        }
        System.out.println("-> BOTÓN LOCAL PRESIONADO: " + calificacion);
        if (controller != null) {
            controller.finalizarTurnoDesdeBotoneraLocal(calificacion);
        }
    }

    public static void detener() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("[+] Puerto local " + serialPort.getSystemPortName() + " cerrado.");
        }
    }
}
