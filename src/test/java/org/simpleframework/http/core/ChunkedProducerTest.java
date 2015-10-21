package org.simpleframework.http.core;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.message.ChunkedConsumer;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ChunkedProducerTest {

    @Test
    public void testChunk() throws Exception {
        testChunk(1024, 1);
        testChunk(1024, 2);
        testChunk(512, 20);
        testChunk(64, 64);
    }

    private void testChunk(int chunkSize, int count) throws Exception {
        final MockSender sender = new MockSender((chunkSize * count) + 1024);
        final MockObserver monitor = new MockObserver();
        final ChunkedConsumer validator = new ChunkedConsumer(new ArrayAllocator());
        final ChunkedEncoder producer = new ChunkedEncoder(monitor, sender);
        final byte[] chunk = new byte[chunkSize];

        for(int i = 0; i < chunk.length; i++) {
            chunk[i] = (byte)String.valueOf(i).charAt(0);
        }
        for(int i = 0; i < count; i++) {
            producer.encode(chunk, 0, chunkSize);
        }
        producer.close();

        System.err.println(sender.getBuffer().encode("UTF-8"));

        final ByteCursor cursor = sender.getCursor();

        while(!validator.isFinished()) {
            validator.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertTrue(monitor.isReady());
    }
}
