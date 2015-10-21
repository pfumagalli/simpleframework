package org.simpleframework.http.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.http.StreamTransport;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportCursor;

public class StreamCursor implements ByteCursor {

    private final TransportCursor cursor;
    private final Transport transport;
    private final byte[] swap;

    public StreamCursor(String source) throws IOException {
        this(source.getBytes("UTF-8"));
    }

    public StreamCursor(byte[] data) throws IOException {
        this(new ByteArrayInputStream(data));
    }

    public StreamCursor(InputStream source) throws IOException {
        this.transport = new StreamTransport(source, new OutputStream() {
            @Override
            public void write(int octet){}
        });
        this.cursor = new TransportCursor(transport);
        this.swap = new byte[1];
    }

    // TODO investigate this
    @Override
    public boolean isOpen() throws IOException {
        return true;
    }

    @Override
    public boolean isReady() throws IOException {
        return cursor.isReady();
    }

    @Override
    public int ready() throws IOException {
        return cursor.ready();
    }

    public int read() throws IOException {
        if(read(swap) > 0) {
            return swap[0] & 0xff;
        }
        return 0;
    }

    @Override
    public int read(byte[] data) throws IOException {
        return read(data, 0, data.length);
    }

    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        return cursor.read(data, off, len);
    }

    @Override
    public int reset(int len) throws IOException {
        return cursor.reset(len);
    }

    @Override
    public void push(byte[] data) throws IOException {
        push(data, 0, data.length);
    }

    @Override
    public void push(byte[] data, int off, int len) throws IOException {
        cursor.push(data, off, len);
    }
}