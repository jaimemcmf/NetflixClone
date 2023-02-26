package com.mykong;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.jetty.server.Server;
import jakarta.ws.rs.client.WebTarget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourceTest {

    private static Server server;
    private static WebTarget target;

    @BeforeAll
    public static void beforeAllTests() {
        server = Main.startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(Main.BASE_URI);
    }

    @AfterAll
    public static void afterAllTests() throws Exception {
        server.stop();
    }





}