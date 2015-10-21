package org.simpleframework.http.core;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.common.lease.Lease;
import org.simpleframework.http.MockTrace;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;


public class MockChannel implements Channel {

    private final ByteCursor cursor;

    public MockChannel(ByteCursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public Trace getTrace(){
        return new MockTrace();
    }

    public Lease getLease() {
        return null;
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
        return new HashMap();
    }

    @Override
    public void close() {}

    @Override
    public SocketChannel getSocket() {
        return null;
    }
}