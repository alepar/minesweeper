package ru.alepar.minesweeper.thirdparty;

import java.io.*;

public class ResourceLauncher {

    public void launch(String resource) {
        exec(unpackResource(resource));
    }

    private static void exec(File fileToExec) {
        try {
            Runtime.getRuntime().exec(fileToExec.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("failed to startup winmine.exe");
        }
    }

    private static File unpackResource(String resource) {
        File dstFile;
        try {
            InputStream is = openResource(resource);
            dstFile = File.createTempFile("winmine", ".exe");
            OutputStream os = new FileOutputStream(dstFile);

            copy(is, os);
        } catch (IOException e) {
            throw new RuntimeException("failed to unpack resource: " + resource);
        }
        return dstFile;
    }

    private static InputStream openResource(String name) {
        InputStream is = WinmineApplication.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new RuntimeException("resource " + name + " not found on classpath");
        }
        return is;
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        try {
            try {
                byte buf[] = new byte[102400];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

}
