package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;

public class MockSender implements ByteWriter {

    private final Buffer buffer;

    public MockSender() {
        this(1024);
    }

    public MockSender(int size) {
        this.buffer = new ArrayBuffer(size);
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public ByteCursor getCursor() throws IOException {
        return new StreamCursor(buffer.encode("UTF-8"));
    }

    @Override
    public void write(byte[] array) throws IOException {
        buffer.append(array);
    }

    @Override
    public void write(byte[] array, int off, int len) throws IOException {
        buffer.append(array, off, len);
    }

    @Override
    public void flush() throws IOException {
        return;
    }

    @Override
    public void close() throws IOException {
        return;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public boolean isOpen() throws Exception {
        return true;
    }

    @Override
    public void write(ByteBuffer source) throws IOException {
        final int mark = source.position();
        final int limit = source.limit();

        final byte[] array = new byte[limit - mark];
        source.get(array, 0, array.length);
        buffer.append(array);
    }

    @Override
    public void write(ByteBuffer source, int off, int len) throws IOException {
        final int mark = source.position();
        final int limit = source.limit();

        if((limit - mark) < len) {
            len = limit - mark;
        }
        final byte[] array = new byte[len];
        source.get(array, 0, len);
        buffer.append(array);
    }
}
