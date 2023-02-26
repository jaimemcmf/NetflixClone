package com.mykong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.mykong.DownloadFile.LOGGER;

public class BucketThread extends Thread{
    String path;
    Boolean remove;
    String fileName;

    BucketThread(String path, Boolean remove, String filename){
        this.path = path;
        this.remove = remove;
        this.fileName = filename;
    }

    public void run(){
        String cmd[];
        if(!remove) {
            cmd = new String[]{"sh", "{path to storeBucket.sh}", path};
        }else{
            cmd = new String[]{"sh", "{path to removeBucket.sh}", fileName};
        }
            LOGGER.info("Executing: {}", (Object) cmd);
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
                LOGGER.info("BucketThread exited with code: {}", exitCode);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
    }
}

