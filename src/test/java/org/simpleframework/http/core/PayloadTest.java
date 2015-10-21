package org.simpleframework.http.core;

import java.util.List;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.Part;
import org.simpleframework.http.message.Header;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.Channel;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PayloadTest {

    private static final String PAYLOAD =
            "POST /index.html HTTP/1.0\r\n"+
                    "Content-Type: multipart/form-data; boundary=AaB03x\r\n"+
                    "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
                    "   \t\t   image/png;\t\r\n\t"+
                    "   q=1.0,*;q=0.1\r\n"+
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
                    "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
                    "\r\n" +
                    "--AaB03x\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file2.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file3.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file3.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: form-data; name='pics'; filename='file4.txt'\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y--\r\n"+
                    "--AaB03x--\r\n";


    @Test
    public void testPayload() throws Exception {
        for(int i = 1; i < 4096; i++) {
            testPayload(i);
        }
    }

    private void testPayload(int dribble) throws Exception {
        final ByteCursor cursor = new DribbleCursor(new StreamCursor(PAYLOAD), 10);
        final Channel channel = new MockChannel(cursor);
        final MockController selector = new MockController();
        final Collector body = new RequestCollector(new ArrayAllocator(), channel);
        final long time = System.currentTimeMillis();

        while(!selector.isReady()) {
            body.collect(selector);
        }
        System.err.println("Time taken to parse payload "+(System.currentTimeMillis() - time)+" ms");

        final Header header = body.getHeader();
        final List<Part> list = body.getBody().getParts();

        AssertJUnit.assertEquals(header.getTarget(), "/index.html");
        AssertJUnit.assertEquals(header.getMethod(), "POST");
        AssertJUnit.assertEquals(header.getMajor(), 1);
        AssertJUnit.assertEquals(header.getMinor(), 0);
        AssertJUnit.assertEquals(header.getContentType().getPrimary(), "multipart");
        AssertJUnit.assertEquals(header.getContentType().getSecondary(), "form-data");
        AssertJUnit.assertEquals(header.getValue("Host"), "some.host.com");
        AssertJUnit.assertEquals(header.getValues("Accept").size(), 4);
        AssertJUnit.assertEquals(header.getValues("Accept").get(0), "image/gif");
        AssertJUnit.assertEquals(header.getValues("Accept").get(1), "image/png");
        AssertJUnit.assertEquals(header.getValues("Accept").get(2), "image/jpeg");
        AssertJUnit.assertEquals(header.getValues("Accept").get(3), "*");
        AssertJUnit.assertEquals(list.size(), 4);
        AssertJUnit.assertEquals(list.get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(0).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");
        AssertJUnit.assertEquals(list.get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(1).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(1).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file2.txt'");
        AssertJUnit.assertEquals(list.get(2).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(2).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(2).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file3.txt'");
        AssertJUnit.assertEquals(list.get(3).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(3).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(3).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file4.txt'");
        AssertJUnit.assertEquals(cursor.ready(), -1);
    }

}
