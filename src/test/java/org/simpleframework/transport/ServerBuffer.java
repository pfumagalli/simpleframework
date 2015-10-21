package org.simpleframework.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;

public class ServerBuffer extends Thread {

    private final ByteArrayOutputStream buffer;
    private final ServerSocket server;
    private final CountDownLatch latch;

    public ServerBuffer() throws Exception {
        this.buffer = new ByteArrayOutputStream();
        this.latch = new CountDownLatch(1);
        this.server = getSocket();
        this.start();
    }

    public ByteArrayOutputStream getBuffer(){
        return buffer;
    }

    public void awaitClose() throws Exception {
        latch.await();
    }

    public int getPort() {
        return server.getLocalPort();
    }

    private ServerSocket getSocket() throws Exception {
        // Scan the ephemeral port range
        for(int i = 2000; i < 10000; i++) { // keep trying to grab the socket
            try {
                final ServerSocket socket = new ServerSocket(i);
                System.out.println("port=["+socket.getLocalPort()+"]");
                return socket;
            } catch(final Exception e) {
                Thread.sleep(200);
            }
        }
        // Scan a second time for good measure, maybe something got freed up
        for(int i = 2000; i < 10000; i++) { // keep trying to grab the socket
            try {
                final ServerSocket socket = new ServerSocket(i);
                System.out.println("port=["+socket.getLocalPort()+"]");
                return socket;
            } catch(final Exception e) {
                Thread.sleep(200);
            }
        }
        throw new IOException("Could not create a client socket");
    }

    @Override
    public void run() {
        try {
            final java.net.Socket socket = server.accept();
            final InputStream in = socket.getInputStream();
            int count = 0;

            while((count = in.read()) != -1) {
                buffer.write(count);
                System.err.write(count);
                System.err.flush();
            }
        } catch(final Exception e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}