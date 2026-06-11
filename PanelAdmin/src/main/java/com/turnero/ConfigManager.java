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

    public static String getIp() {
        File file = getConfigFile();
        if (!file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String ip = props.getProperty(IP_KEY);
            return (ip != null && !ip.trim().isEmpty()) ? ip.trim() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveIp(String ip) {
        Properties props = new Properties();
        try {
            File file = getConfigFile();
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }
            }
            props.setProperty(IP_KEY, ip.trim());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Configuracion del Sistema de Turnos");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
