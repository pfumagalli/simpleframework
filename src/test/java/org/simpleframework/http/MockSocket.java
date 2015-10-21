
package org.simpleframework.http;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.Socket;
import org.simpleframework.transport.trace.Trace;

public class MockSocket implements Socket {

    private final SocketChannel socket;
    private final SSLEngine engine;
    private final Map map;

    public MockSocket(SocketChannel socket) {
        this(socket, null);
    }

    public MockSocket(SocketChannel socket, SSLEngine engine) {
        this.map = new HashMap();
        this.engine = engine;
        this.socket = socket;
    }

    @Override
    public SSLEngine getEngine() {
        return engine;
    }

    @Override
    public SocketChannel getChannel() {
        return socket;
    }

    @Override
    public Map getAttributes() {
        return map;
    }

    @Override
    public Trace getTrace() {
        return new MockTrace();
    }
}

