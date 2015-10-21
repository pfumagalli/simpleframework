package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class TokenConsumerTest {

    @Test
    public void testTokenConsumer() throws IOException {
        final Allocator allocator = new ArrayAllocator();
        final TokenConsumer consumer = new TokenConsumer(allocator, "\r\n".getBytes());
        final ByteCursor cursor = new StreamCursor("\r\n");

        consumer.consume(cursor);

        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertTrue(consumer.isFinished());
    }

    @Test
    public void testTokenConsumerException() throws IOException {
        final Allocator allocator = new ArrayAllocator();
        final TokenConsumer consumer = new TokenConsumer(allocator, "\r\n".getBytes());
        final ByteCursor cursor = new StreamCursor("--\r\n");
        boolean exception = false;

        try {
            consumer.consume(cursor);
        } catch(final Exception e) {
            exception = true;
        }
        AssertJUnit.assertTrue("Exception not thrown for invalid token", exception);
    }

    @Test
    public void testTokenConsumerDribble() throws IOException {
        final Allocator allocator = new ArrayAllocator();
        final TokenConsumer consumer = new TokenConsumer(allocator, "This is a large token to be consumed\r\n".getBytes());
        final DribbleCursor cursor = new DribbleCursor(new StreamCursor("This is a large token to be consumed\r\n0123456789"), 1);

        consumer.consume(cursor);

        AssertJUnit.assertEquals(cursor.ready(), 1);
        AssertJUnit.assertTrue(consumer.isFinished());
        AssertJUnit.assertEquals(cursor.read(), '0');
        AssertJUnit.assertEquals(cursor.read(), '1');
        AssertJUnit.assertEquals(cursor.read(), '2');
    }

}
