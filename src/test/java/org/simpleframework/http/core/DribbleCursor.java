package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.transport.ByteCursor;

public class DribbleCursor implements ByteCursor {

    private final ByteCursor cursor;
    private final byte[] swap;
    private final int dribble;

    public DribbleCursor(ByteCursor cursor, int dribble) {
        this.cursor = cursor;
        this.dribble = dribble;
        this.swap = new byte[1];
    }

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
        final int ready = cursor.ready();

        return Math.min(ready, dribble);
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
        final int size = Math.min(len, dribble);

        return cursor.read(data, off, size);
    }

    @Override
    public int reset(int len) throws IOException {
        return cursor.reset(len);
    }

    @Override
    public void push(byte[] data) throws IOException {
        cursor.push(data);
    }

    @Override
    public void push(byte[] data, int off, int len) throws IOException {
        cursor.push(data, off, len);
    }
}
