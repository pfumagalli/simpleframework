package org.simpleframework.http.core;

import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.testng.annotations.Test;

public class StopTest {

    private static final int ITERATIONS = 20;

    @Test
    public void testStop() throws Exception {
        final ThreadDumper dumper = new ThreadDumper();

        dumper.start();
        dumper.waitUntilStarted();

        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        final int initialThreads = threadBean.getThreadCount();

        for(int i = 0; i < ITERATIONS; i++) {
            try {
                final ServerCriteria criteria = createServer();
                final InetSocketAddress address = criteria.getAddress();
                final Connection connection = criteria.getConnection();
                final Client client = createClient(address, String.format("[%s of %s]", i, ITERATIONS));

                Thread.sleep(2000); // allow some requests to execute
                connection.close();
                Thread.sleep(100); // ensure client keeps executing
                client.close();
                Thread.sleep(1000); // wait for threads to terminate
            }catch(final Exception e) {
                e.printStackTrace();
            }
            //assertEquals(initialThreads, threadBean.getThreadCount());
        }
        dumper.kill();
    }

    public static Client createClient(InetSocketAddress address, String tag) throws Exception {
        final ConcurrentExecutor executor = new ConcurrentExecutor(Runnable.class, 20);
        final int port = address.getPort();
        final Client client = new Client(executor, port, tag);

        client.start();
        return client;
    }

    public static ServerCriteria createServer() throws Exception {
        final Container container = new Container() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    final PrintStream out = response.getPrintStream();
                    response.setValue("Content-Type", "text/plain");
                    response.setValue("Connection", "close");

                    out.print("TEST " + new Date());
                    response.close();
                }catch(final Exception e) {
                    e.printStackTrace();
                    try {
                        response.close();
                    }catch(final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        final ContainerSocketProcessor server = new ContainerSocketProcessor(container);
        final Connection connection = new SocketConnection(server);
        final InetSocketAddress address = (InetSocketAddress)connection.connect(null); // ephemeral port

        return new ServerCriteria(connection, address);
    }

    private static class Client extends Thread implements Closeable {

        private final ConcurrentExecutor executor;
        private final RequestTask task;
        private volatile boolean dead;

        public Client(ConcurrentExecutor executor, int port, String tag) {
            this.task = new RequestTask(this, port, tag);
            this.executor = executor;
        }

        public boolean isDead() {
            return dead;
        }

        @Override
        public void run() {
            try {
                while(!dead) {
                    executor.execute(task);
                    Thread.sleep(100);
                }
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() {
            dead = true;
            executor.stop();
        }
    }

    private static class RequestTask implements Runnable {

        private final Client client;
        private final String tag;
        private final int port;

        public RequestTask(Client client, int port, String tag) {
            this.client = client;
            this.port = port;
            this.tag = tag;
        }

        @Override
        public void run() {
            try {
                if(!client.isDead()) {
                    final URL target = new URL("http://localhost:"+port+"/");
                    final URLConnection connection = target.openConnection();

                    // set a timeout
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    final InputStream stream = connection.getInputStream();
                    final StringBuilder builder = new StringBuilder();
                    int octet = 0;

                    while((octet = stream.read()) != -1) {
                        builder.append((char)octet);
                    }
                    stream.close();
                    System.out.println(tag + " " + Thread.currentThread() + ": " + builder);
                }
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ServerCriteria {

        private final Connection connection;
        private final InetSocketAddress address;

        public ServerCriteria(Connection connection, InetSocketAddress address){
            this.connection = connection;
            this.address = address;
        }
        public Connection getConnection() {
            return connection;
        }
        public InetSocketAddress getAddress() {
            return address;
        }
    }
}
