package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoveryService implements Runnable {

    private static final Logger logger =
            Logger.getLogger(DiscoveryService.class.getName());

    private static final int DISCOVERY_PORT = 8888;
    private static final int SERVER_PORT = 8080;

    @Override
    public void run() {

        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {

            logger.info("Discovery service started on UDP port " + DISCOVERY_PORT);

            byte[] buffer = new byte[1024];

            while (!Thread.currentThread().isInterrupted()) {

                DatagramPacket request =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(request);

                String message = new String(
                        request.getData(),
                        0,
                        request.getLength(),
                        StandardCharsets.UTF_8);

                logger.info("Received discovery request from "
                        + request.getAddress().getHostAddress());

                if ("DISCOVER_SERVICE".equals(message)) {

                    String port = System.getenv("PORT");
                    if (port == null || port.isBlank()) {
                        port = String.valueOf(SERVER_PORT);
                    }

                    byte[] responseBytes = port.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket response =
                            new DatagramPacket(
                                    responseBytes,
                                    responseBytes.length,
                                    request.getAddress(),
                                    request.getPort());

                    socket.send(response);

                    logger.info("Discovery response sent.");
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Discovery service failed", e);
        }
    }
}