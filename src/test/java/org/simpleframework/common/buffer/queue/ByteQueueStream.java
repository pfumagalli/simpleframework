package org.simpleframework.common.buffer.queue;

import java.io.IOException;
import java.io.InputStream;

public class ByteQueueStream extends InputStream {

    private final ByteQueue queue;

    public ByteQueueStream(ByteQueue queue) {
        this.queue = queue;
    }

    @Override
    public int read() throws IOException {
        final byte[] array = new byte[1];
        final int count = read(array) ;

        if(count != -1) {
            return array[0] & 0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return queue.read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int off, int size) throws IOException {
        return queue.read(buffer, off, size);
    }

    @Override
    public int available() throws IOException {
        return queue.available();
    }

    @Override
    public void close() throws IOException {
        queue.close();
    }
}
