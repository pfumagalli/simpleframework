package org.simpleframework.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.FileAllocator;
import org.simpleframework.http.core.Client.AnonymousTrustManager;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.CertificateChallenge;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class MockRenegotiationServer implements Container {

    private final ConfigurableCertificateServer server;
    private final Connection connection;
    private final SocketAddress address;
    private final SSLContext context;
    private final TraceAnalyzer agent;

    public static void main(String[] list) throws Exception {
        System.err.println("Starting renegotiation test.....");
        //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        //System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
        //File file = new File("C:\\work\\development\\async_http\\yieldbroker-proxy-site\\etc\\www.yieldbroker.com.pfx");
        final File file = new File("/Users/niallg/Work/development/yieldbroker/proxy/yieldbroker-proxy-site/certificate/www.yieldbroker.com.pfx");
        //File file = new File("C:\\work\\development\\async_http\\yieldbroker-proxy-trading\\etc\\uat.yieldbroker.com.pfx");
        final KeyStoreReader reader = new KeyStoreReader(KeyStoreType.PKCS12, file, "p", "p");
        final SecureSocketContext context = new SecureSocketContext(reader, SecureProtocol.TLS);
        final SSLContext sslContext = context.getContext();
        final MockRenegotiationServer server = new MockRenegotiationServer(sslContext, false, 10001);
        server.start();
    }

    public MockRenegotiationServer(SSLContext context, boolean certRequired, int port) throws IOException {
        final Allocator allocator = new FileAllocator();
        final ContainerTransportProcessor processor = new ContainerTransportProcessor(this, allocator, 4);
        final TransportGrabber grabber = new TransportGrabber(processor);
        final TransportSocketProcessor processorServer = new TransportSocketProcessor(grabber);

        this.server = new ConfigurableCertificateServer(processorServer, certRequired);
        this.agent = new ConsoleAgent();
        this.connection = new SocketConnection(server, agent);
        this.address = new InetSocketAddress(port);
        this.context = context;
    }

    @Override
    public void handle(final Request req, final Response resp) {
        boolean challengeForCertificate  = false;

        try {
            final PrintStream out = resp.getPrintStream();
            final String normal = req.getPath().getPath();

            if(normal.indexOf(".ico") == -1) {
                final SSLEngine engine = (SSLEngine)req.getAttribute(SSLEngine.class);
                if(normal.startsWith("/niall/cert")) {
                    final SocketChannel channel = ((Transport)req.getAttribute(Transport.class)).getChannel();
                    System.err.println("NEW REQUEST ("+System.identityHashCode(engine)+"): " + req);


                    try {
                        resp.setContentType("text/plain");
                        resp.setValue("Connection", "keep-alive");
                        String certificateInfo = null;


                        try {
                            final X509Certificate[] list = req.getClientCertificate().getChain();
                            final StringBuilder builder = new StringBuilder();
                            for(final X509Certificate cert : list) {
                                final X509Certificate x509 = cert;
                                builder.append(x509);
                            }
                            certificateInfo = builder.toString();
                        } catch(final Exception e) {
                            e.printStackTrace();
                            certificateInfo = e.getMessage();
                            challengeForCertificate = true;

                            // http://stackoverflow.com/questions/14281628/ssl-renegotiation-with-client-certificate-causes-server-buffer-overflow
                            // Perhaps an expect 100 continue does something here?????
                            if(challengeForCertificate) {
                                final Certificate certificate = req.getClientCertificate();
                                final CertificateChallenge challenge = certificate.getChallenge();

                                final Future<Certificate> future = challenge.challenge(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.err.println("FINISHED THE CHALLENGE!!!");
                                    }
                                });
                                final Certificate futureCert = future.get(10, TimeUnit.SECONDS);

                                if(futureCert == null) {
                                    System.err.println("FAILED TO GET CERT!!!!");
                                } else {
                                    System.err.println("**** GOT THE CERT");
                                }

                                String text=  "Challenge finished without cert";
                                try {
                                    final X509Certificate[] list = req.getClientCertificate().getChain();
                                    final StringBuilder builder = new StringBuilder();
                                    for(final X509Certificate x509 : list) {
                                        builder.append(x509);
                                    }
                                    text = builder.toString();
                                } catch(final Exception ex) {
                                    e.printStackTrace();
                                }
                                out.print(text);
                                out.flush();
                                try {
                                    resp.close();
                                } catch(final Exception ex){
                                    e.printStackTrace();
                                }
                            }
                        }
                        //  Thread.sleep(10000);
                        if(!challengeForCertificate) {
                            try {
                                final X509Certificate[] list = req.getClientCertificate().getChain();
                                final StringBuilder builder = new StringBuilder();
                                for(final X509Certificate cert : list) {
                                    final X509Certificate x509 = cert;
                                    builder.append(x509);
                                }
                                certificateInfo = builder.toString();
                            } catch(final Exception e) {
                                e.printStackTrace();
                            }
                            out.print(certificateInfo);
                            out.flush();
                            resp.close();
                        }


                    } finally {
                        if(!challengeForCertificate) {
                            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!("+System.identityHashCode(engine)+")!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WORKING");
                        }
                    }
                } else {
                    resp.setStatus(org.simpleframework.http.Status.NOT_FOUND);
                    resp.setValue("Connection", "keep-alive");
                    resp.setValue("Content-Type", "text/plain");
                    out.println("Ok normal request with NO renegotiation " + req);
                }
            } else {
                resp.setStatus(org.simpleframework.http.Status.NOT_FOUND);
                resp.setValue("Connection", "keep-alive");
                resp.setValue("Content-Type", "text/plain");
                out.println("fuck off!!");
            }
        } catch(final Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(!challengeForCertificate) {
                    resp.close();
                } else {
                    System.err.println("NOT DOING ANYTHING WITH THE REQUEST, AS A CHALLENGE IS UNDERWAY challengeForCertificate="+challengeForCertificate+" path="+req);
                }
            } catch(final Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public void start() throws IOException {
        connection.connect(address, context);
    }

    public void stop() throws IOException {
        connection.close();
    }

    private static class ConsoleAgent implements TraceAnalyzer {

        private final Map<SelectableChannel, Integer> map;
        private final AtomicInteger count;

        public ConsoleAgent() {
            this.map = new ConcurrentHashMap<SelectableChannel, Integer>();
            this.count = new AtomicInteger();
        }

        @Override
        public Trace attach(SelectableChannel channel) {
            if(map.containsKey(channel)) {
                throw new IllegalStateException("Can't attach twice");
            }
            final int counter = count.getAndIncrement();
            map.put(channel, counter);

            return new Trace() {

                @Override
                public void trace(Object event) {
                    trace(event, "");
                }

                @Override
                public void trace(Object event, Object value) {
                    if(value instanceof Throwable) {
                        final StringWriter writer = new StringWriter();
                        final PrintWriter out = new PrintWriter(writer);
                        ((Exception)value).printStackTrace(out);
                        out.flush();
                        value = writer.toString();
                    }
                    if((value != null) && !String.valueOf(value).isEmpty()) {
                        System.err.printf("(%s) [%s] %s: %s%n", Thread.currentThread().getName(), counter, event, value);
                    } else {
                        System.err.printf("(%s) [%s] %s%n", Thread.currentThread().getName(), counter, event);
                    }
                }
            };
        }

        @Override
        public void stop() {
            System.err.println("Stop agent");
        }
    }

    public static class TransportGrabber implements TransportProcessor {

        private final TransportProcessor processor;

        public TransportGrabber(TransportProcessor processor) {
            this.processor = processor;
        }

        @Override
        public void process(Transport transport) throws IOException {
            transport.getAttributes().put(Transport.class, transport);
            processor.process(transport);

        }

        @Override
        public void stop() throws IOException {
            processor.stop();
        }

    }

    public static class ConfigurableCertificateServer implements SocketProcessor {

        private final SocketProcessor server;
        private boolean certRequired;

        public ConfigurableCertificateServer(SocketProcessor server) {
            this(server, false);
        }

        public ConfigurableCertificateServer(SocketProcessor server, boolean certRequired) {
            this.certRequired = certRequired;
            this.server = server;
        }

        public void setCertRequired(boolean certRequired) {
            this.certRequired = certRequired;
        }

        @Override
        public void process(Socket socket) throws IOException {
            final SSLEngine engine = socket.getEngine();
            socket.getAttributes().put(SSLEngine.class, engine);
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
