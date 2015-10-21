package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.message.FixedLengthConsumer;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FixedConsumerTest implements Allocator {

    private Buffer buffer;

    @Override
    public Buffer allocate() {
        return buffer;
    }

    @Override
    public Buffer allocate(long size) {
        return buffer;
    }

    @Test
    public void testConsumer() throws Exception {
        testConsumer(10, 10, 10);
        testConsumer(1024, 10, 1024);
        testConsumer(1024, 1024, 1024);
        testConsumer(1024, 1024, 1023);
        testConsumer(1024, 1, 1024);
        testConsumer(1, 1, 1);
        testConsumer(2, 2, 2);
        testConsumer(3, 1, 2);
    }

    private void testConsumer(int entitySize, int dribble, int limitSize) throws Exception {
        final StringBuffer buf = new StringBuffer();

        // Ensure that we dont try read forever
        limitSize = Math.min(entitySize, limitSize);

        for(int i = 0, line = 0; i < entitySize; i++) {
            final String text = "["+String.valueOf(i)+"]";

            line += text.length();
            buf.append(text);

            if(line >= 48) {
                buf.append("\n");
                line = 0;
            }

        }
        buffer = new ArrayAllocator().allocate();

        final String requestBody = buf.toString();
        final FixedLengthConsumer consumer = new FixedLengthConsumer(this, limitSize);
        final ByteCursor cursor = new DribbleCursor(new StreamCursor(requestBody), dribble);
        final byte[] requestBytes = requestBody.getBytes("UTF-8");

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        final byte[] consumedBytes = buffer.encode("UTF-8").getBytes("UTF-8");

        AssertJUnit.assertEquals(buffer.encode("UTF-8").length(), limitSize);

        for(int i = 0; i < limitSize; i++) {
            if(consumedBytes[i] != requestBytes[i]) {
                throw new IOException("Fixed consumer modified the request!");
            }
        }
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
