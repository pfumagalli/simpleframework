package org.simpleframework.common.buffer.queue;

import java.io.InputStream;

import org.testng.annotations.Test;

public class BufferQueueTest {

    @Test
    public void testBufferQueue() throws Exception {
        final ByteQueue queue = new ArrayByteQueue(1024 * 1000);
        final BufferQueue buffer = new BufferQueue(queue);

        final Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InputStream source = buffer.open();
                    for(int i = 0; i < 1000; i++) {
                        final int octet = source.read();
                        System.err.write(octet);
                        System.err.flush();
                    }
                }catch(final Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final Thread writer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(int i = 0; i < 1000; i++) {
                        buffer.append(("Test message: "+i+"\n").getBytes());
                    }
                }catch(final Exception e) {
                    e.printStackTrace();
                }
            }
        });
        reader.start();
        writer.start();
        reader.join();
        writer.join();
    }

}
