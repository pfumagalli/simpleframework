package org.simpleframework.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

import org.simpleframework.http.core.Client.AnonymousTrustManager;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportCursor;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.TransportWriter;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class RenegotiationExample {

    public static void main(String[] list) throws Exception {
        final Connection serverCon = createServer(false, 443);
        /*SSLSocket socketCon = createClient();
      OutputStream out = socketCon.getOutputStream();

      for(int i = 0; i < 1000; i++) {
         out.write("TEST".getBytes());
         out.flush();
         Thread.sleep(5000);
      }*/
        Thread.sleep(1000000);
        serverCon.close();
    }

    public static Connection createServer(boolean certificateRequired, int listenPort) throws Exception {
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
        final File file = new File("C:\\work\\development\\async_http\\yieldbroker-proxy-trading\\etc\\uat.yieldbroker.com.pfx");
        final KeyStoreReader reader = new KeyStoreReader(KeyStoreType.PKCS12, file, "p", "p");
        final SecureSocketContext context = new SecureSocketContext(reader, SecureProtocol.TLS);
        final SSLContext sslContext = context.getContext();
        final TraceAnalyzer agent = new MockAgent();
        final TransportProcessor processor = new MockTransportProcessor();
        final TransportSocketProcessor server = new TransportSocketProcessor(processor);
        final ConfigurableCertificateServer certServer = new ConfigurableCertificateServer(server);
        final SocketConnection con = new SocketConnection(certServer, agent);
        final SocketAddress serverAddress = new InetSocketAddress(listenPort);

        certServer.setCertRequired(certificateRequired);
        con.connect(serverAddress, sslContext);

        return con;
    }


    public static SSLSocket createClient() throws Exception {
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
        final File file = new File("C:\\work\\development\\async_http\\yieldbroker-proxy-benchmark\\etc\\niall.pfx");
        final KeyStoreReader reader = new KeyStoreReader(KeyStoreType.PKCS12, file, "1234", "1234");
        final SecureSocketContext context = new SecureSocketContext(reader, SecureProtocol.TLS);
        final SocketFactory factory = context.getSocketFactory();
        final SSLSocket socket = (SSLSocket)factory.createSocket("localhost", 9333);
        socket.setEnabledProtocols(new String[] {"SSLv3", "TLSv1"});

        return socket;
    }

    public static class ConfigurableCertificateServer implements SocketProcessor {

        private final SocketProcessor server;
        private boolean certRequired;

        public ConfigurableCertificateServer(SocketProcessor server) {
            this.server = server;
        }

        public void setCertRequired(boolean certRequired) {
            this.certRequired = certRequired;
        }

        @Override
        public void process(Socket socket) throws IOException {
            if(certRequired) {
                socket.getEngine().setNeedClientAuth(true);
            }
            server.process(socket);
        }

        @Override
        public void stop() throws IOException {
            System.err.println("stop");
        }
    }

    public static class TransportPoller extends Thread {

        private final ByteCursor cursor;
        private final Transport transport;


        public TransportPoller(Transport transport) {
            this.cursor = new TransportCursor(transport);
            this.transport = transport;
        }

        @Override
        public void run() {
            try {
                System.err.println("Poller started");
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final byte[] chunk = new byte[1024];
                int count = 0;

                while(cursor.isOpen()) {
                    while(cursor.isReady()) {
                        count = cursor.read(chunk);
                        if(count != 0) {
                            out.write(chunk, 0, count);
                        }
                    }
                    final String message = out.toString();
                    out.reset();
                    if((message != null) && !message.isEmpty()) {
                        final SSLEngine engine = transport.getEngine();
                        String certificateInfo = null;

                        if(engine != null) {
                            try {
                                final Certificate[] list = engine.getSession().getPeerCertificates();
                                final StringBuilder builder = new StringBuilder();
                                for(final Certificate cert : list) {
                                    final X509Certificate x509 = (X509Certificate)cert;
                                    builder.append(x509);
                                }
                                certificateInfo = builder.toString();
                            } catch(final Exception e) {

                                // Here we would have to ask the transport to renegotiate.....!!!
                                transport.getEngine().setWantClientAuth(true);
                                transport.getEngine().beginHandshake();
                                transport.getEngine().setWantClientAuth(true);
                                for(int i = 0; i < 100; i++) {
                                    final Runnable task = transport.getEngine().getDelegatedTask();
                                    if(task != null){
                                        task.run();
                                    }
                                }
                                certificateInfo = e.getMessage();
                            }
                        }
                        final TransportWriter sender = new TransportWriter(transport);
                        sender.write(
                                ("HTTP/1.1 200 OK\r\n" +
                                        "Connection: keep-alive\r\n"+
                                        "Content-Length: 5\r\n"+
                                        "Content-Type: text/plain\r\n"+
                                        "\r\n"+
                                        "hello").getBytes());


                        sender.flush();




                        System.err.println("["+message+"]: " + certificateInfo);
                    }
                    Thread.sleep(5000);
                }
            } catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static class MockTransportProcessor implements TransportProcessor {

        @Override
        public void process(Transport transport) throws IOException {
            System.err.println("New transport");
            final TransportPoller poller = new TransportPoller(transport);
            poller.start();
        }

        @Override
        public void stop() throws IOException {
            System.err.println("Transport stopped");
        }
    }

    private static class MockAgent implements TraceAnalyzer {

        @Override
        public Trace attach(SelectableChannel channel) {
            return new Trace() {
                @Override
                public void trace(Object event) {
                    trace(event, "");
                }
                @Override
                public void trace(Object event, Object value) {
                    if((value != null) && !String.valueOf(value).isEmpty()) {
                        System.err.printf("%s: %s%n", event, value);
                    } else {
                        System.err.println(event);
                    }
                }
            };
        }

        @Override
        public void stop() {
            System.err.println("Stop agent");
        }

    }

    public enum KeyStoreType {
        JKS("JKS", "SunX509"),
        PKCS12("PKCS12", "SunX509");

        private final String algorithm;
        private final String type;

        private KeyStoreType(String type, String algorithm) {
            this.algorithm = algorithm;
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public KeyStore getKeyStore() throws KeyStoreException {
            return KeyStore.getInstance(type);
        }

        public KeyManagerFactory getKeyManagerFactory() throws NoSuchAlgorithmException {
            return KeyManagerFactory.getInstance(algorithm);
        }
    }

    private static class KeyStoreManager {

        private final KeyStoreType keyStoreType;

        public KeyStoreManager(KeyStoreType keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public KeyManager[] getKeyManagers(InputStream keyStoreSource, String keyStorePassword, String keyManagerPassword) throws Exception {
            final KeyStore keyStore = keyStoreType.getKeyStore();
            final KeyManagerFactory keyManagerFactory = keyStoreType.getKeyManagerFactory();

            keyStore.load(keyStoreSource, keyManagerPassword.toCharArray());
            keyManagerFactory.init(keyStore, keyManagerPassword.toCharArray());

            return keyManagerFactory.getKeyManagers();
        }

    }

    private static class KeyStoreReader {

        private final KeyStoreManager keyStoreManager;
        private final String keyManagerPassword;
        private final String keyStorePassword;
        private final File keyStore;

        public KeyStoreReader(KeyStoreType keyStoreType, File keyStore, String keyStorePassword, String keyManagerPassword) {
            this.keyStoreManager = new KeyStoreManager(keyStoreType);
            this.keyManagerPassword = keyManagerPassword;
            this.keyStorePassword = keyStorePassword;
            this.keyStore = keyStore;
        }

        public KeyManager[] getKeyManagers() throws Exception {
            final InputStream storeSource = new FileInputStream(keyStore);

            try {
                return keyStoreManager.getKeyManagers(storeSource, keyStorePassword, keyManagerPassword);
            } finally {
                storeSource.close();
            }
        }
    }

    public enum SecureProtocol {
        DEFAULT("Default"),
        SSL("SSL"),
        TLS("TLS");

        private final String protocol;

        private SecureProtocol(String protocol) {
            this.protocol = protocol;
        }

        public SSLContext getContext() throws NoSuchAlgorithmException {
            return SSLContext.getInstance(protocol);
        }
    }

    private static class SecureSocketContext {

        private final X509TrustManager trustManager;
        private final X509TrustManager[] trustManagers;
        private final KeyStoreReader keyStoreReader;
        private final SecureProtocol secureProtocol;

        public SecureSocketContext(KeyStoreReader keyStoreReader, SecureProtocol secureProtocol) {
            this.trustManager = new AnonymousTrustManager();
            this.trustManagers = new X509TrustManager[]{trustManager};
            this.keyStoreReader = keyStoreReader;
            this.secureProtocol = secureProtocol;
        }

        public SSLContext getContext() throws Exception {
            final KeyManager[] keyManagers = keyStoreReader.getKeyManagers();
            final SSLContext secureContext = secureProtocol.getContext();

            secureContext.init(keyManagers, trustManagers, null);

            return secureContext;
        }

        public SocketFactory getSocketFactory() throws Exception {
            final KeyManager[] keyManagers = keyStoreReader.getKeyManagers();
            final SSLContext secureContext = secureProtocol.getContext();

            secureContext.init(keyManagers, trustManagers, null);

            return secureContext.getSocketFactory();
        }
    }

}
