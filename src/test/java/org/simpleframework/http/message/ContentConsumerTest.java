package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ContentConsumerTest implements Allocator {

    private static final byte[] BOUNDARY = { 'A', 'a', 'B', '0', '3', 'x' };

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
    public void testContent() throws Exception {
        testContent(1, 1);

        for(int i = 1; i < 1000; i++) {
            testContent(i, i);
        }
        for(int i = 20; i < 1000; i++) {
            for(int j = 1; j < 19; j++) {
                testContent(i, j);
            }
        }
        testContent(10, 10);
        testContent(100, 2);
    }

    @Test
    public void testContent(int entitySize, int dribble) throws Exception {
        final MockSegment segment = new MockSegment();
        final PartData list = new PartData();
        final ContentConsumer consumer = new ContentConsumer(this, segment, list, BOUNDARY);
        final StringBuffer buf = new StringBuffer();

        segment.add("Content-Disposition", "form-data; name='photo'; filename='photo.jpg'");
        segment.add("Content-Type", "text/plain");
        segment.add("Content-ID", "<IDENTITY>");

        for(int i = 0, line = 0; buf.length() < entitySize; i++) {
            final String text = String.valueOf(i);

            line += text.length();
            buf.append(text);

            if(line >= 48) {
                buf.append("\n");
                line = 0;
            }
        }
        // Get request body without boundary
        final String requestBody = buf.toString();

        // Add the boundary to the request body
        buf.append("\r\n--");
        buf.append(new String(BOUNDARY, 0, BOUNDARY.length, "UTF-8"));
        buffer = new ArrayAllocator().allocate();

        final DribbleCursor cursor = new DribbleCursor(new StreamCursor(buf.toString()), dribble);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        final byte[] consumedBytes = buffer.encode("UTF-8").getBytes("UTF-8");
        final String consumedBody = new String(consumedBytes, 0, consumedBytes.length, "UTF-8");

        AssertJUnit.assertEquals(String.format("Failed for entitySize=%s and dribble=%s", entitySize, dribble), consumedBody, requestBody);
        AssertJUnit.assertEquals(cursor.read(), '\r');
        AssertJUnit.assertEquals(cursor.read(), '\n');
        AssertJUnit.assertEquals(cursor.read(), '-');
        AssertJUnit.assertEquals(cursor.read(), '-');
        AssertJUnit.assertEquals(cursor.read(), BOUNDARY[0]);
        AssertJUnit.assertEquals(cursor.read(), BOUNDARY[1]);
        AssertJUnit.assertEquals(consumer.getPart().getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(consumer.getPart().getContentType().getSecondary(), "plain");
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub

    }



}
