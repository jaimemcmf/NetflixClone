package com.mykong;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final String BASE_URI = "http://{ip}:{port}/";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static Server startServer() {

        // scan packages

        final ResourceConfig config = new ResourceConfig(DownloadFile.class, UploadFile.class, UserCon.class, SearchMovie.class, Streaming.class, DeleteMovie.class);
        final Server server =
                JettyHttpContainerFactory.createServer(URI.create(BASE_URI), config);

        return server;

    }

    public static void main(String[] args) {

        try {
            final Server server = startServer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop();
                } catch (Exception e) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }
            }));


            // block and wait shut down signal, like CTRL+C
            Thread.currentThread().join();

        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
