package com.mykong;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;

import static com.mykong.DownloadFile.LOGGER;

public class LowRes extends Thread{
    String fileName;
    String ext;
    String ProjPath;

    String fileLocation;

    int id;

    LowRes(String fileName, String ext, String projPath, int id){
        this.fileName = fileName;
        this.ext = ext;
        this.ProjPath = projPath;
        this.id = id;

    }

    public void run(){

        LOGGER.info("Starting Conversion");
        try {
            FFmpeg ffmpeg;
            FFprobe ffprobe;
            String input, output;
            // Pahts in the windows FS
            if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                ffmpeg = new FFmpeg("{path to ffmpeg.exe}");
                ffprobe = new FFprobe("{path to ffmprobe.exe}");
                input = ProjPath + "\\src\\main\\resources\\videos\\High\\" + fileName + "_" + id + ext;
                output = ProjPath + "\\src\\main\\resources\\videos\\Low\\" + fileName + "_360_" + id + ext;
            } else{
                ffmpeg = new FFmpeg("ffmpeg");
                ffprobe = new FFprobe("ffprobe");
                input = ProjPath + "/src/main/resources/videos/High/" + fileName + "_" + id + ext;
                output = ProjPath + "/src/main/resources/videos/Low/" + fileName + "_360_" + id + ext;
            }

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(input)     // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists


                    .addOutput(output)   // Filename for the destination
                    .setFormat("mp4")        // Format is inferred from filename, or can be set

                    .disableSubtitle()       // No subtiles

                    .setAudioChannels(1)         // Mono audio
                    .setAudioCodec("aac")        // using the aac codec
                    .setAudioSampleRate(48_000)  // at 48KHz
                    .setAudioBitRate(32768)      // at 32 kbit/s

                    .setVideoCodec("libx264")     // Video using x264
                    .setVideoFrameRate(24, 1)     // at 24 frames per second
                    .setVideoResolution(480, 360) // at 640x480 resolution

                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            File f = new File(output);
            if(f.exists()) {
                LOGGER.info("Low resolution version of {} was successfully created", fileName);
                BucketThread bucketThread = new BucketThread(output, false, fileName);
                bucketThread.start();
            }
                else LOGGER.info("An error has occurred while generating the low resolution file for {}", fileName);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
