package client;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoveryClient {

    private static final Logger logger =
            Logger.getLogger(DiscoveryClient.class.getName());

    private static final int DISCOVERY_PORT = 8888;

    public ServerDetails discoverServer() {

        try (DatagramSocket socket = new DatagramSocket()) {

            socket.setBroadcast(true);
            socket.setSoTimeout(5000);

            byte[] request =
                    "DISCOVER_SERVICE".getBytes(StandardCharsets.UTF_8);

            InetAddress broadcast =
                    InetAddress.getByName("255.255.255.255");

            DatagramPacket packet =
                    new DatagramPacket(
                            request,
                            request.length,
                            broadcast,
                            DISCOVERY_PORT);

            socket.send(packet);

            logger.info("Discovery broadcast sent.");

            byte[] responseBuffer = new byte[1024];

            DatagramPacket response =
                    new DatagramPacket(
                            responseBuffer,
                            responseBuffer.length);

            socket.receive(response);

            logger.info("Discovery response received.");

            String port =
                    new String(
                            response.getData(),
                            0,
                            response.getLength(),
                            StandardCharsets.UTF_8);

            return new ServerDetails(
                    response.getAddress().getHostAddress(),
                    port.trim());

        } catch (SocketTimeoutException e) {

            logger.warning("No server found.");

        } catch (IOException e) {

            logger.log(Level.SEVERE,
                    "Discovery failed",
                    e);
        }

        return null;
    }
}