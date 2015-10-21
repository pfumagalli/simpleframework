package org.simpleframework.http.message;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PartSeriesConsumerTest {

    private static final String SIMPLE =
            "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt ...\r\n"+
                    "--AaB03x--\r\n";

    private static final String NORMAL =
            "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file2.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file2.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file3.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file3.txt ...\r\n"+
                    "--AaB03x--\r\n";

    private static final String MIXED =
            "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file2.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file2.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file3.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file3.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file4.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y--\r\n"+
                    "--AaB03x--\r\n";

    @Test
    public void testSimple() throws Exception {
        final PartData list = new PartData();
        final PartSeriesConsumer consumer = new PartSeriesConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"));
        final ByteCursor cursor = new StreamCursor(SIMPLE);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        AssertJUnit.assertEquals(list.getParts().size(), 1);
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
        AssertJUnit.assertEquals(list.getParts().get(0).getContent(), "example contents of file1.txt ...");
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(consumer.getBody().getContent(), SIMPLE);
    }

    @Test
    public void testNormal() throws Exception {
        final PartData list = new PartData();
        final PartSeriesConsumer consumer = new PartSeriesConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"));
        final ByteCursor cursor = new StreamCursor(NORMAL);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        AssertJUnit.assertEquals(list.getParts().size(), 3);
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
        AssertJUnit.assertEquals(list.getParts().get(0).getContent(), "example contents of file1.txt");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(1).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file2.txt'");
        AssertJUnit.assertEquals(list.getParts().get(1).getContent(), "example contents of file2.txt");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(2).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file3.txt'");
        AssertJUnit.assertEquals(list.getParts().get(2).getContent(), "example contents of file3.txt ...");
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(consumer.getBody().getContent(), NORMAL);
    }

    @Test
    public void testMixed() throws Exception {
        final PartData list = new PartData();
        final PartSeriesConsumer consumer = new PartSeriesConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"));
        final ByteCursor cursor = new StreamCursor(MIXED);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        AssertJUnit.assertEquals(list.getParts().size(), 4);
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
        AssertJUnit.assertEquals(list.getParts().get(0).getContent(), "example contents of file1.txt");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(1).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file2.txt'");
        AssertJUnit.assertEquals(list.getParts().get(1).getContent(), "example contents of file2.txt ...");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(2).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file3.txt'");
        AssertJUnit.assertEquals(list.getParts().get(2).getContent(), "example contents of file3.txt ...");
        AssertJUnit.assertEquals(list.getParts().get(3).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(3).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(3).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file4.txt'");
        AssertJUnit.assertEquals(list.getParts().get(3).getContent(), "example contents of file4.txt ...");
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(consumer.getBody().getContent(), MIXED);
    }

    @Test
    public void testDribble() throws Exception {
        final PartData list = new PartData();
        final PartSeriesConsumer consumer = new PartSeriesConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"));
        final ByteCursor cursor = new DribbleCursor(new StreamCursor(NORMAL), 1);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        AssertJUnit.assertEquals(list.getParts().size(), 3);
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
        AssertJUnit.assertEquals(list.getParts().get(0).getContent(), "example contents of file1.txt");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(1).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(1).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file2.txt'");
        AssertJUnit.assertEquals(list.getParts().get(1).getContent(), "example contents of file2.txt");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(2).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(2).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file3.txt'");
        AssertJUnit.assertEquals(list.getParts().get(2).getContent(), "example contents of file3.txt ...");
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(consumer.getBody().getContent(), NORMAL);
    }

    public static void main(String[] list) throws Exception {
        new PartSeriesConsumerTest().testMixed();
    }
}