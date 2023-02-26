package com.mykong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.mykong.DownloadFile.LOGGER;

public class StreamThread extends Thread {

    int videoId;
    String MoviePath;
    Boolean High;

    StreamThread(int videoId, String moviePath, Boolean High){
        this.videoId = videoId;
        this.MoviePath = moviePath;
        this.High = High;
    }
    //  ffmpeg -re -i example-vid.mp4 -vcodec libx264 -vprofile baseline -g 30 -acodec aac -strict -2 -f flv rtmp://localhost/show/stream
    public void run() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String[] cmd;
        if(High) {
            cmd = new String[]{"sh", "{path to watch_stream.sh}", MoviePath, "" + videoId};
        } else {
            cmd = new String[]{"sh", "{path to watch_stream.sh}", MoviePath, "" + videoId+"_360"};
        }
        processBuilder.command(cmd);
        try{
            Process process = processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }

            int exitCode = process.waitFor();
            LOGGER.info("StreamThread exited with code: {}", exitCode);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
