package org.simpleframework.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.FileAllocator;
import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.http.core.ThreadDumper;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ConnectionTest {

    private static final int PING_TEST_PORT = 12366;

    @Test
    public void testSocketPing() throws Exception {
        // for(int i = 0; i < 10; i++) {
        //    System.err.printf("Ping [%s]%n", i);
        //    testPing(PING_TEST_PORT, "Hello World!", true, 2);
        // }
    }

    @Test
    public void testURLPing() throws Exception {
        for(int i = 0; i < 20; i++) {
            System.err.printf("Ping [%s]%n", i);
            testPing(PING_TEST_PORT, "Hello World!", false, 10);
        }
    }

    @Test
    public void testMixPing() throws Exception {
        //for(int i = 0; i < 50; i+=2) {
        //   System.err.printf("Ping [%s]%n", i);
        //   testPing(PING_TEST_PORT, "Hello World!", true, 2);
        //   System.err.printf("Ping [%s]%n", i+1);
        //   testPing(PING_TEST_PORT, "Hello World!", false, 10);
        //}
    }

    @Test(enabled = false)
    private void testPing(int port, String message, boolean socket, int count) throws Exception {
        final PingServer server = new PingServer(PING_TEST_PORT, message);
        final Pinger pinger = new Pinger(PING_TEST_PORT, socket, count);

        server.start();
        final List<String> list = pinger.execute();

        for(int i = 0; i < count; i++) { // at least 20
            final String result = list.get(i);

            AssertJUnit.assertNotNull(result);
            AssertJUnit.assertEquals(result, message);
        }
        server.stop();
        pinger.validate();
        pinger.stop(); // wait for it all to finish
    }

    private static class DebugServer implements SocketProcessor {

        private final SocketProcessor server;

        public DebugServer(SocketProcessor server) {
            this.server = server;
        }

        @Override
        public void process(Socket socket) throws IOException {
            System.err.println("Connect...");
            server.process(socket);
        }

        @Override
        public void stop() throws IOException {
            System.err.println("Stop...");
            server.stop();
        }
    }

    private static class PingServer implements Container {

        private final Connection connection;
        private final SocketAddress address;
        private final String message;

        public PingServer(int port, String message) throws Exception {
            final Allocator allocator = new FileAllocator();
            final TransportProcessor processor  = new ContainerTransportProcessor(this, allocator, 5);
            final SocketProcessor server = new TransportSocketProcessor(processor);
            final DebugServer debug = new DebugServer(server);

            this.connection = new SocketConnection(debug);
            this.address = new InetSocketAddress(port);
            this.message = message;
        }

        public void start() throws Exception {
            try {
                System.err.println("Starting...");
                connection.connect(address);
            }finally {
                System.err.println("Started...");
            }
        }

        public void stop() throws Exception {
            connection.close();
        }

        @Override
        public void handle(Request req, Response resp) {
            try {
                System.err.println(req);
                final PrintStream out = resp.getPrintStream(1024);

                resp.setValue("Content-Type", "text/plain");
                out.print(message);
                out.close();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Pinger implements Runnable {

        private final int count;
        private final int port;
        private final boolean socket;
        private final CountDownLatch latch;
        private final CountDownLatch stop;
        private final ConcurrentExecutor executor;
        private final ThreadDumper dumper;
        private final List<String> list;
        private final List<java.net.Socket> sockets;

        public Pinger(int port, boolean socket, int count) throws Exception {
            this.executor = new ConcurrentExecutor(Pinger.class, count);
            this.list = new Vector<String>(count);
            this.sockets = new Vector<java.net.Socket>(count);
            this.latch = new CountDownLatch(count);
            this.stop = new CountDownLatch(count + count);
            this.dumper = new ThreadDumper();
            this.port = port;
            this.socket = socket;
            this.count = count;
        }

        public List<String> execute() throws Exception {
            dumper.start();

            for(int i = 0; i < count; i++) {
                executor.execute(this);
            }
            latch.await();

            // Overrun with pings to ensure they close
            if(socket) {
                for(int i = 0; i < count; i++) {
                    executor.execute(this);
                }
            }
            return list;
        }

        public void validate() throws Exception {
            if(socket) {
                for(final java.net.Socket socket : sockets) {
                    if(socket.getInputStream().read() != -1) {
                        throw new IOException("Connection not closed");
                    } else {
                        System.err.println("Socket is closed");
                    }
                }
            }
        }

        public void stop() throws Exception {
            executor.stop();

            if(socket) {
                stop.await(); // wait for all excess pings to finish
            }
            dumper.kill();
        }

        private String ping() throws Exception {
            if(socket) {
                return pingWithSocket();
            }
            return pingWithURL();
        }

        @Override
        public void run() {
            try {
                final String result = ping();

                list.add(result);
                latch.countDown();
            }catch(final Throwable e){
                System.err.println(e);
            } finally {
                stop.countDown(); // account for excess pings
            }
        }

        /**
         * This works as it opens a socket and sends the request.
         * This will split using the CRLF and CRLF ending.
         *
         * @return the response body
         *
         * @throws Exception if the socket can not connect
         */
        private String pingWithSocket() throws Exception {
            final java.net.Socket socket = new java.net.Socket("localhost", port);
            final OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET / HTTP/1.1\r\n" +
                            "Host: localhost\r\n"+
                            "\r\n").getBytes());
            out.flush();
            final InputStream in = socket.getInputStream();
            final byte[] block = new byte[1024];
            final int count = in.read(block);
            final String result = new String(block, 0, count);
            final String parts[] = result.split("\r\n\r\n");

            if(!result.startsWith("HTTP")) {
                throw new IOException("Header is not valid");
            }
            sockets.add(socket);
            return parts[1];
        }

        /**
         * Use the standard URL tool to get the content.
         *
         * @return the response body
         *
         * @throws Exception if a connection can not be made.
         */
        private String pingWithURL() throws Exception {
            final URL target = new URL("http://localhost:"+ port+"/");
            final InputStream in = target.openStream();
            final byte[] block = new byte[1024];
            final int count = in.read(block);
            final String result = new String(block, 0, count);

            return result;
        }
    }
}
