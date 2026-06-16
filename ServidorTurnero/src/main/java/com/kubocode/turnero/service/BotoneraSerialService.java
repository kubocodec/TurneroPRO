package com.kubocode.turnero.service;

import com.fazecast.jSerialComm.SerialPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class BotoneraSerialService {

    @Value("${botonera.puerto:COM3}")
    private String puertoConfigurado;

    @Value("${botonera.puesto:1}")
    private Integer puestoId;

    @Autowired
    private ITurnoService turnoService;

    private SerialPort serialPort;
    private Thread pollingThread;
    private volatile boolean running = false;

    public void setPuestoId(Integer puestoId) {
        this.puestoId = puestoId;
        System.out.println("Botonera ahora configurada para el puesto: " + puestoId);
    }

    @PostConstruct
    public void iniciarBotonera() {
        if ("DESHABILITADO".equalsIgnoreCase(puertoConfigurado) || "NONE".equalsIgnoreCase(puertoConfigurado)) {
            System.out.println("[*] Botonera centralizada en servidor deshabilitada.");
            return;
        }

        System.out.println("==========================================");
        System.out.println("   INICIANDO BOTONERA FÍSICA (JAVA)       ");
        System.out.println("==========================================");

        try {
            serialPort = SerialPort.getCommPort(puertoConfigurado);
            serialPort.setBaudRate(9600);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
            serialPort.setParity(SerialPort.NO_PARITY);
            // Habilitar DTR y RTS (importante para que el microcontrolador despierte)
            serialPort.setDTR();
            serialPort.setRTS();

            if (!serialPort.openPort()) {
                System.err.println("No se pudo abrir el puerto " + puertoConfigurado + ". Verifique que no esté en uso.");
                return;
            }

            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 500, 500);

            System.out.println("[+] Puerto " + puertoConfigurado + " abierto correctamente desde Spring Boot.");
            
            // Pausa de 1s para permitir el reinicio por DTR
            Thread.sleep(1000);

            System.out.println("[+] Enviando secuencia de DESPERTAR (AA, FF, 41, 61, 45)...");
            byte[] wakeupSequence = {(byte) 0xAA, (byte) 0xFF, (byte) 0x41, (byte) 0x61, (byte) 0x45};
            for (byte b : wakeupSequence) {
                serialPort.writeBytes(new byte[]{b}, 1);
                Thread.sleep(100);
            }
            
            System.out.println("[+] Botonera activada. Iniciando lectura en hilo secundario...");
            
            // Iniciar hilo de polling
            running = true;
            pollingThread = new Thread(this::pollingLoop);
            pollingThread.setDaemon(true);
            pollingThread.start();

        } catch (Exception e) {
            System.err.println("Error al inicializar la botonera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pollingLoop() {
        while (running && serialPort != null && serialPort.isOpen()) {
            try {
                // Enviar comando 'R' para solicitar lectura
                byte[] rCommand = {'R'};
                serialPort.writeBytes(rCommand, 1);

                Thread.sleep(100); // Pequeña pausa para permitir respuesta

                // Leer datos disponibles
                int bytesAvailable = serialPort.bytesAvailable();
                if (bytesAvailable > 0) {
                    byte[] readBuffer = new byte[bytesAvailable];
                    int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                    
                    // Procesamos cada byte leído
                    for (int i = 0; i < numRead; i++) {
                        char c = (char) readBuffer[i];
                        procesarBoton(c);
                    }
                }

                Thread.sleep(400); // 500ms total loop (100+400)
            } catch (Exception e) {
                if (running) {
                    System.err.println("Error en el loop de lectura serial: " + e.getMessage());
                }
            }
        }
    }

    private void procesarBoton(char boton) {
        String calificacion = null;
        switch (boton) {
            case '1': // Según prueba: Botón 1 reportaba EXCELENTE pero el usuario quiere MALO (invertido)
                calificacion = "MALO";
                break;
            case '2': 
                calificacion = "REGULAR";
                break;
            case '3': 
                calificacion = "BUENO";
                break;
            case '4': 
                calificacion = "EXCELENTE";
                break;
            default:
                // Ignorar otros bytes basura
                return;
        }

        System.out.println("-> BOTON PRESIONADO FÍSICAMENTE: " + calificacion);
        
        try {
            turnoService.finalizarAtencionPorPuesto(puestoId, calificacion);
            System.out.println("-> Turno para el puesto " + puestoId + " calificado como " + calificacion + ".");
        } catch (Exception e) {
            System.err.println("Error al finalizar el turno desde botonera: " + e.getMessage());
        }
    }

    @PreDestroy
    public void cerrarBotonera() {
        System.out.println("Cerrando botonera física...");
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Puerto serial cerrado.");
        }
    }
}
