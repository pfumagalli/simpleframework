package org.simpleframework.http.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.http.socket.service.DirectRouter;
import org.simpleframework.http.socket.service.Router;
import org.simpleframework.http.socket.service.RouterContainer;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class WebSocketChatApplication implements Container, TransportProcessor {

    private final WebSocketCertificate certificate;
    private final Router negotiator;
    private final RouterContainer container;
    private final SocketAddress address;
    private final Connection connection;
    private final TransportProcessor processor;
    private final Allocator allocator;
    private final SocketProcessor server;

    public WebSocketChatApplication(WebSocketChatRoom service, WebSocketCertificate certificate, TraceAnalyzer agent, int port) throws Exception {
        this.negotiator = new DirectRouter(service);
        this.container = new RouterContainer(this, negotiator, 10);
        this.allocator = new ArrayAllocator();
        this.processor = new ContainerTransportProcessor(container, allocator, 1);
        this.server = new TransportSocketProcessor(this);
        this.connection = new SocketConnection(server, agent);
        this.address = new InetSocketAddress(port);
        this.certificate = certificate;
    }

    public void connect() throws Exception {
        // if(certificate != null) {
        //    SSLContext context = certificate.getContext();
        //
        //    connection.connect(address, context);
        //    container.start();
        // } else {
        connection.connect(address);
        // }
    }

    @Override
    public void handle(Request req, Response resp) {
        System.err.println(req);

        if(req.getTarget().equals("/")) {
            final long time = System.currentTimeMillis();

            try {
                resp.setDate("Date", time);
                resp.setValue("Server", "WebSocketChatApplication/1.0");
                resp.setContentType("text/html");
                final String page = loadPage("WebSocketChatLogin.html");

                resp.setDate("Date", time);
                resp.setValue("Server", "WebSocketChatApplication/1.0");
                resp.setContentType("text/html");

                final PrintStream out = resp.getPrintStream();
                out.println(page);
                out.close();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        } else if(req.getTarget().equals("/login")) {
            final String user = req.getParameter("user");
            final long time = System.currentTimeMillis();

            try {
                resp.setStatus(Status.FOUND);
                resp.setValue("Location", "/chat");
                resp.setCookie("user", user);
                resp.setDate("Date", time);
                resp.setValue("Server", "WebSocketChatApplication/1.0");
                resp.setContentType("text/html");
                resp.close();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        } else if(req.getTarget().equals("/chat")) {
            final long time = System.currentTimeMillis();

            try {
                final Cookie user = req.getCookie("user");
                final String name = user.getValue();
                String page = loadPage("WebSocketChatRoom.html");

                resp.setDate("Date", time);
                resp.setValue("Server", "WebSocketChatApplication/1.0");
                resp.setContentType("text/html");

                final PrintStream out = resp.getPrintStream();
                page = page.replaceAll("%1", name);
                out.println(page);
                out.close();
            } catch(final Exception e) {
                e.printStackTrace();
            }
        } else if(req.getTarget().equals("/talk")){
            final long time = System.currentTimeMillis();
            try {
                container.handle(req, resp);
            } catch(final Exception e) {
                e.printStackTrace();
            }
        } else {
            final long time = System.currentTimeMillis();

            try {
                resp.setCode(404);
                resp.setDescription("Not Found");
                resp.setDate("Date", time);
                resp.setValue("Server", "WebSocketChatApplication/1.0");
                resp.setContentType("text/plain");

                final PrintStream out = resp.getPrintStream();

                out.println("Not Found");
                out.close();
            } catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String loadPage(String name) throws IOException {
        final InputStream loginPage = WebSocketChatApplication.class.getResourceAsStream(name);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] chunk = new byte[1024];
        int count = 0;

        while((count = loginPage.read(chunk)) != -1) {
            out.write(chunk, 0, count);
        }
        out.close();
        return out.toString();
    }

    @Override
    public void process(Transport transport) throws IOException {
        final Map map = transport.getAttributes();
        map.put(Transport.class, transport);
        processor.process(transport);
    }

    @Override
    public void stop() throws IOException {
        processor.stop();
    }
}
