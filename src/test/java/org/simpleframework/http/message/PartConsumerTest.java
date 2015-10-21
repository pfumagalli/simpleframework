package org.simpleframework.http.message;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PartConsumerTest {

    private static final String SOURCE =
            "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "... contents of file1.txt ...\r\n"+
                    "--AaB03x\r\n";

    @Test
    public void testHeader() throws Exception {
        final PartData list = new PartData();
        final PartConsumer consumer = new PartConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"), 8192);
        final ByteCursor cursor = new StreamCursor(SOURCE);

        while(!consumer.isFinished()) {
            consumer.consume(cursor);
        }
        AssertJUnit.assertEquals(list.getParts().size(), 1);
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.getParts().get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
    }
}
