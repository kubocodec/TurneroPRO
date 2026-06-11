package com.kubocode.turnero.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LicenseManager {

    private static final String SECRET_SALT = "KuboCodeTurnero$$2026-SuperSecure";
    private static final String LICENSE_FILE_NAME = "license.key";

    public static String getHardwareId() {
        String hwid = "";
        try {
            // Utilizamos wmic csproduct get uuid para obtener el ID de la placa principal en Windows.
            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "csproduct", "get", "uuid"});
            process.getOutputStream().close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("uuid")) {
                    hwid = line;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback en caso de que wmic no devuelva nada
        if (hwid == null || hwid.isEmpty()) {
            hwid = System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version");
        }

        return hwid;
    }

    public static String generateExpectedLicenseCode(String hwid) {
        try {
            String input = hwid + SECRET_SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            // Devolver un trozo más corto de la licencia, dividido en bloques para que sea manejable.
            String fullHash = hexString.toString().toUpperCase();
            return String.format("%s-%s-%s-%s", 
                fullHash.substring(0, 4), 
                fullHash.substring(4, 8), 
                fullHash.substring(8, 12), 
                fullHash.substring(12, 16));
                
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar la licencia", e);
        }
    }

    public static boolean isLicenseValid() {
        try {
            Path path = Paths.get(getAppDataFolder(), LICENSE_FILE_NAME);
            if (!Files.exists(path)) {
                return false;
            }
            String storedLicense = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            String currentHwid = getHardwareId();
            String expectedLicense = generateExpectedLicenseCode(currentHwid);

            return storedLicense.equals(expectedLicense);
        } catch (IOException e) {
            return false;
        }
    }

    public static void saveLicense(String licenseCode) throws IOException {
        File folder = new File(getAppDataFolder());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Path path = Paths.get(getAppDataFolder(), LICENSE_FILE_NAME);
        Files.write(path, licenseCode.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String getAppDataFolder() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + "AppData" + File.separator + "Local" + File.separator + "KuboCode" + File.separator + "SistemaTurnero";
    }
}
