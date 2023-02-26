package com.mykong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.mykong.DownloadFile.LOGGER;

public class GenerateThumb extends Thread {

    String moviePath;
    String thumbnail;

    GenerateThumb(String moviePath, String thumbnail){
        this.moviePath = moviePath;
        this.thumbnail = thumbnail;
    }

    public void run(){
        LOGGER.info("Launching Thread to generate Thumbnail");
        String[] cmd = new String[]{"sh", "{path to genThumb.sh}", moviePath, thumbnail};
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd);
        try {
            Process process = processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }

            int exitCode = process.waitFor();
            LOGGER.info("GenThumb exited with code: {}", exitCode);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
