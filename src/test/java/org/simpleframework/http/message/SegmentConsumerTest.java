package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SegmentConsumerTest {

    private static final String SOURCE =
            "Content-Type: application/x-www-form-urlencoded\r\n"+
                    "User-Agent:\r\n" +
                    "Content-Length: 42\r\n"+
                    "Transfer-Encoding: chunked\r\n"+
                    "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
                    "   \t\t   image/png;\t\r\n\t"+
                    "   q=1.0,*;q=0.1\r\n"+
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
                    "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
                    "\r\n";

    private static final String EMPTY =
            "Accept-Language:\r\n"+
                    "Content-Length:\r\n"+
                    "Content-Type:\r\n"+
                    "Content-Disposition:\r\n"+
                    "Transfer-Encoding:\r\n"+
                    "Expect:\r\n"+
                    "Cookie:\r\n"+
                    "\r\n";

    protected SegmentConsumer header;

    @BeforeMethod
    public void setUp() throws IOException {
        header = new SegmentConsumer();
    }

    @Test
    public void testHeader() throws Exception {
        final ByteCursor cursor = new StreamCursor(SOURCE);

        while(!header.isFinished()) {
            header.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(header.getValue("Pragma"), null);
        AssertJUnit.assertEquals(header.getValue("User-Agent"), "");
        AssertJUnit.assertEquals(header.getValue("Content-Length"), "42");
        AssertJUnit.assertEquals(header.getValue("Content-Type"), "application/x-www-form-urlencoded");
        AssertJUnit.assertEquals(header.getValue("Host"), "some.host.com");
        AssertJUnit.assertEquals(header.getValues("Accept").size(), 4);
        AssertJUnit.assertEquals(header.getValues("Accept").get(0), "image/gif");
        AssertJUnit.assertEquals(header.getValues("Accept").get(1), "image/png");
        AssertJUnit.assertEquals(header.getValues("Accept").get(2), "image/jpeg");
        AssertJUnit.assertEquals(header.getValues("Accept").get(3), "*");
        AssertJUnit.assertEquals(header.getContentType().getPrimary(), "application");
        AssertJUnit.assertEquals(header.getContentType().getSecondary(), "x-www-form-urlencoded");
        AssertJUnit.assertEquals(header.getTransferEncoding(), "chunked");
    }

    @Test
    public void testEmptyHeader() throws Exception {
        final ByteCursor cursor = new StreamCursor(EMPTY);

        while(!header.isFinished()) {
            header.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(header.getValue("Accept-Language"), "");
        AssertJUnit.assertEquals(header.getValue("Content-Length"), "");
        AssertJUnit.assertEquals(header.getValue("Content-Type"), "");
        AssertJUnit.assertEquals(header.getValue("Content-Disposition"), "");
        AssertJUnit.assertEquals(header.getValue("Transfer-Encoding"), "");
        AssertJUnit.assertEquals(header.getValue("Expect"), "");
        AssertJUnit.assertEquals(header.getValue("Cookie"), "");
        AssertJUnit.assertEquals(header.getContentType().getPrimary(), null);
        AssertJUnit.assertEquals(header.getContentType().getSecondary(), null);
    }

    @Test
    public void testDribble() throws Exception {
        final ByteCursor cursor = new DribbleCursor(new StreamCursor(SOURCE), 1);

        while(!header.isFinished()) {
            header.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(header.getValue("Content-Length"), "42");
        AssertJUnit.assertEquals(header.getValue("Content-Type"), "application/x-www-form-urlencoded");
        AssertJUnit.assertEquals(header.getValue("Host"), "some.host.com");
        AssertJUnit.assertEquals(header.getValues("Accept").size(), 4);
        AssertJUnit.assertEquals(header.getValues("Accept").get(0), "image/gif");
        AssertJUnit.assertEquals(header.getValues("Accept").get(1), "image/png");
        AssertJUnit.assertEquals(header.getValues("Accept").get(2), "image/jpeg");
        AssertJUnit.assertEquals(header.getValues("Accept").get(3), "*");
        AssertJUnit.assertEquals(header.getContentType().getPrimary(), "application");
        AssertJUnit.assertEquals(header.getContentType().getSecondary(), "x-www-form-urlencoded");
        AssertJUnit.assertEquals(header.getTransferEncoding(), "chunked");
    }
}
