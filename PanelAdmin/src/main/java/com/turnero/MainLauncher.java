package com.turnero;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class MainLauncher {

    private static final String LOCK_FILE = System.getProperty("java.io.tmpdir") + File.separator + "PanelAdmin.lock";
    private static FileLock lock;
    private static FileChannel channel;
    private static RandomAccessFile raf;

    public static void main(String[] args) {
        if (!tryLock()) {
            JOptionPane.showMessageDialog(null,
                    "La aplicación ya se encuentra en ejecución.\nNo es posible abrir más de una instancia.",
                    "Panel Admin - Ya en ejecución",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
            return;
        }

        // Liberar lock al cerrar la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(MainLauncher::releaseLock));

        LoginView.main(args);
    }

    private static boolean tryLock() {
        try {
            raf = new RandomAccessFile(LOCK_FILE, "rw");
            channel = raf.getChannel();
            lock = channel.tryLock();
            return lock != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static void releaseLock() {
        try {
            if (lock != null) lock.release();
            if (channel != null) channel.close();
            if (raf != null) raf.close();
            new File(LOCK_FILE).delete();
        } catch (Exception ignored) {}
    }
}
