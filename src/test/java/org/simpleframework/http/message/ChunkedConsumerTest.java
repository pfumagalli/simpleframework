package org.simpleframework.http.message;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.core.Chunker;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChunkedConsumerTest implements Allocator {

    public Buffer buffer;

    @BeforeMethod
    public void setUp() {
        buffer = new ArrayBuffer();
    }

    @Override
    public Buffer allocate() {
        return buffer;
    }

    @Override
    public Buffer allocate(long size) {
        return buffer;
    }

    @Test
    public void testChunks() throws Exception {
        testChunks(64, 1024, 64);
        testChunks(64, 11, 64);
        testChunks(1024, 1024, 100000);
        testChunks(1024, 10, 100000);
        testChunks(1024, 11, 100000);
        testChunks(1024, 113, 100000);
        testChunks(1024, 1, 100000);
        testChunks(1024, 2, 50000);
        testChunks(1024, 3, 50000);
        testChunks(10, 1024, 50000);
        testChunks(1, 10, 71234);
        testChunks(2, 11, 123456);
        testChunks(15, 113, 25271);
        testChunks(16, 1, 43265);
        testChunks(64, 2, 63266);
        testChunks(32, 3, 9203);
    }

    @Test
    public void testChunks(int chunkSize, int dribble, int entitySize) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream plain = new ByteArrayOutputStream();
        final Chunker encode = new Chunker(out);
        final StringBuffer buf = new StringBuffer();
        int fill = 0;

        for(int i = 0, line = 0; i < entitySize; i++) {
            final String text = "["+String.valueOf(i)+"]";

            if(fill >= chunkSize) {
                encode.write(buf.toString().getBytes("UTF-8"));
                plain.write(buf.toString().getBytes("UTF-8"));
                buf.setLength(0);
                fill = 0;
                line = 0;
            }
            line += text.length();
            fill += text.length();
            buf.append(text);

            if(line >= 48) {
                buf.append("\n");
                fill++;
                line = 0;
            }

        }
        if(buf.length() > 0) {
            encode.write(buf.toString().getBytes("UTF-8"));
            plain.write(buf.toString().getBytes("UTF-8"));
        }
        buffer = new ArrayAllocator().allocate(); // N.B clear previous buffer
        encode.close();
        final byte[] data = out.toByteArray();
        final byte[] plainText = plain.toByteArray();
        //System.out.println(">>"+new String(data, 0, data.length, "UTF-8")+"<<");
        //System.out.println("}}"+new String(plainText, 0, plainText.length,"UTF-8")+"{{");
        final DribbleCursor cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(data)), dribble);
        final ChunkedConsumer test = new ChunkedConsumer(this);

        while(!test.isFinished()) {
            test.consume(cursor);
        }
        final byte[] result = buffer.encode("UTF-8").getBytes("UTF-8");
        //System.out.println("))"+new String(result, 0, result.length, "UTF-8")+"((");

        if(result.length != plainText.length) {
            throw new IOException(String.format("Bad encoding result=[%s] plainText=[%s]", result.length, plainText.length));
        }
        for(int i = 0; i < result.length; i++) {
            if(result[i] != plainText[i]) {
                throw new IOException(String.format("Values do not match for %s, %s, and %s", chunkSize, dribble, entitySize));
            }
        }
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub

    }


}
