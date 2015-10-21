package org.simpleframework.http.core;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class WebSocketUpgradeTest implements Container {

    private static final String OPEN_HANDSHAKE =
            "GET /chat HTTP/1.1\r\n"+
                    "Host: server.example.com\r\n"+
                    "Upgrade: websocket\r\n"+
                    "Connection: Upgrade\r\n"+
                    "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n"+
                    "Origin: http://example.com\r\n"+
                    "Sec-WebSocket-Protocol: chat, superchat\r\n"+
                    "Sec-WebSocket-Version: 14\r\n" +
                    "\r\n";

    public static class MockChannel implements Channel {

        private final ByteCursor cursor;

        public MockChannel(StreamCursor cursor, int dribble) {
            this.cursor = new DribbleCursor(cursor, dribble);
        }
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public Trace getTrace() {
            return new MockTrace();
        }

        @Override
        public Certificate getCertificate() {
            return null;
        }

        @Override
        public ByteCursor getCursor() {
            return cursor;
        }

        @Override
        public ByteWriter getWriter() {
            return new MockSender();
        }

        @Override
        public Map getAttributes() {
            return null;
        }

        @Override
        public void close() {}

        @Override
        public SocketChannel getSocket() {
            return null;
        }
    }

    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<Response>();

    @Test
    public void testWebSocketUpgrade() throws Exception {
        final Allocator allocator = new ArrayAllocator();
        final Controller handler = new ContainerController(this, allocator, 10, 2);
        final StreamCursor cursor = new StreamCursor(OPEN_HANDSHAKE);
        final Channel channel = new MockChannel(cursor, 10);

        handler.start(channel);

        final Response response = responses.poll(5000, TimeUnit.MILLISECONDS);

        AssertJUnit.assertEquals(response.getValue("Connection"), "Upgrade");
        AssertJUnit.assertEquals(response.getValue("Upgrade"), "websocket");
        AssertJUnit.assertTrue(response.isCommitted());
        AssertJUnit.assertTrue(response.isKeepAlive());
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            process(request, response);
            responses.offer(response);
        }catch(final Exception e) {
            e.printStackTrace();
            AssertJUnit.assertTrue(false);
        }
    }

    public void process(Request request, Response response) throws Exception {
        final String method = request.getMethod();

        AssertJUnit.assertEquals(method, "GET");
        AssertJUnit.assertEquals(request.getValue("Upgrade"), "websocket");
        AssertJUnit.assertEquals(request.getValue("Connection"), "Upgrade");
        AssertJUnit.assertEquals(request.getValue("Sec-WebSocket-Key"), "dGhlIHNhbXBsZSBub25jZQ==");

        response.setCode(101);
        response.setValue("Connection", "close");
        response.setValue("Upgrade", "websocket");

        final OutputStream out = response.getOutputStream();

        out.write(10); // force commit

        AssertJUnit.assertTrue(response.isCommitted());
        AssertJUnit.assertTrue(response.isKeepAlive());
    }

    public static void main(String[] list) throws Exception {
        new ReactorProcessorTest().testMinimal();
    }

}
