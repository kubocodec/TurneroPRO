package com.turnero;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final String APP_FOLDER = System.getProperty("user.home") + File.separator + ".turnero";
    private static final String CONFIG_FILE_NAME = "panel_admin_config.properties";
    private static final String IP_KEY = "server.ip";

    private static File getConfigFile() {
        File folder = new File(APP_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, CONFIG_FILE_NAME);
    }

    private static synchronized Properties loadProperties() {
        File file = getConfigFile();
        Properties props = new Properties();
        boolean changed = false;

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Ensure default properties exist
        if (!props.containsKey("botonera.puerto")) {
            props.setProperty("botonera.puerto", "COM3");
            changed = true;
        }
        if (!props.containsKey("botonera.habilitada")) {
            props.setProperty("botonera.habilitada", "false");
            changed = true;
        }
        if (!props.containsKey("puestos.maximo")) {
            props.setProperty("puestos.maximo", "21");
            changed = true;
        }

        if (changed) {
            saveProperties(props);
        }

        return props;
    }

    private static synchronized void saveProperties(Properties props) {
        File file = getConfigFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Configuracion del Sistema de Turnos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIp() {
        Properties props = loadProperties();
        String ip = props.getProperty(IP_KEY);
        return (ip != null && !ip.trim().isEmpty()) ? ip.trim() : null;
    }

    public static void saveIp(String ip) {
        Properties props = loadProperties();
        props.setProperty(IP_KEY, ip.trim());
        saveProperties(props);
    }

    public static String getBotoneraPuerto() {
        Properties props = loadProperties();
        String puerto = props.getProperty("botonera.puerto", "COM3");
        return (puerto != null && !puerto.trim().isEmpty()) ? puerto.trim() : "COM3";
    }

    public static boolean isBotoneraHabilitada() {
        Properties props = loadProperties();
        String hab = props.getProperty("botonera.habilitada", "false");
        return Boolean.parseBoolean(hab);
    }

    public static void saveBotoneraConfig(String puerto, boolean habilitada) {
        Properties props = loadProperties();
        props.setProperty("botonera.puerto", puerto.trim());
        props.setProperty("botonera.habilitada", String.valueOf(habilitada));
        saveProperties(props);
    }

    public static int getMaxPuestos() {
        Properties props = loadProperties();
        String maxStr = props.getProperty("puestos.maximo", "21");
        try {
            return Integer.parseInt(maxStr.trim());
        } catch (Exception e) {
            return 21;
        }
    }
}
