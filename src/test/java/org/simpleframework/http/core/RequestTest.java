package org.simpleframework.http.core;

import java.util.List;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.Channel;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class RequestTest {

    private static final String HEADER =
            "POST /index.html?a=b&c=d&e=f&g=h&a=1 HTTP/1.0\r\n"+
                    "Content-Type: multipart/form-data; boundary=AaB03x\r\n"+
                    "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
                    "   \t\t   image/png;\t\r\n\t"+
                    "   q=1.0,*;q=0.1\r\n"+
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
                    "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
                    "\r\n";

    private static final String BODY =
            "--AaB03x\r\n"+
                    "Content-Disposition: file; name=\"file1\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"file2\"; filename=\"file2.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file2.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"file3\"; filename=\"file3.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file3.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"file4\"; filename=\"file4.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y--\r\n"+
                    "--AaB03x--\r\n";

    private static final byte[] PAYLOAD = (HEADER + BODY).getBytes();

    @Test
    public void testPayload() throws Exception {
        final long start = System.currentTimeMillis();

        for(int i = 1; i < 8192; i++) {
            testPayload(i);
        }
        System.err.printf("time=%s%n",(System.currentTimeMillis() - start));
    }

    @Test
    public void testPerformance() throws Exception {
        final long start = System.currentTimeMillis();

        for(int i = 1; i < 10000; i++) {
            testPayload(8192);
        }
        System.err.printf("time=%s%n",(System.currentTimeMillis() - start));
    }

    private void testPayload(int dribble) throws Exception {
        System.out.println("Testing dribbling cursor of "+dribble+" ...");
        ByteCursor cursor = new StreamCursor(PAYLOAD);

        if(dribble < PAYLOAD.length) {
            cursor = new DribbleCursor(cursor, dribble);
        }
        final Channel channel = new MockChannel(cursor);
        final MockController selector = new MockController();
        final Collector body = new RequestCollector(new ArrayAllocator(), channel);

        while(!selector.isReady()) {
            body.collect(selector);
        }
        final Request request = new RequestEntity(null, body);
        final List<Part> list = request.getParts();

        AssertJUnit.assertEquals(request.getParameter("a"), "b");
        AssertJUnit.assertEquals(request.getParameter("c"), "d");
        AssertJUnit.assertEquals(request.getParameter("e"), "f");
        AssertJUnit.assertEquals(request.getParameter("g"), "h");
        AssertJUnit.assertEquals(request.getTarget(), "/index.html?a=b&c=d&e=f&g=h&a=1");
        AssertJUnit.assertEquals(request.getMethod(), "POST");
        AssertJUnit.assertEquals(request.getMajor(), 1);
        AssertJUnit.assertEquals(request.getMinor(), 0);
        AssertJUnit.assertEquals(request.getContentType().getPrimary(), "multipart");
        AssertJUnit.assertEquals(request.getContentType().getSecondary(), "form-data");
        AssertJUnit.assertEquals(request.getValue("Host"), "some.host.com");
        AssertJUnit.assertEquals(request.getValues("Accept").size(), 4);
        AssertJUnit.assertEquals(request.getValues("Accept").get(0), "image/gif");
        AssertJUnit.assertEquals(request.getValues("Accept").get(1), "image/png");
        AssertJUnit.assertEquals(request.getValues("Accept").get(2), "image/jpeg");
        AssertJUnit.assertEquals(request.getValues("Accept").get(3), "*");
        AssertJUnit.assertEquals(request.getCookie("UID").getValue(), "1234-5678");
        AssertJUnit.assertEquals(request.getCookie("UID").getPath(), "/");
        AssertJUnit.assertEquals(request.getCookie("UID").getDomain(), ".host.com");
        AssertJUnit.assertEquals(request.getCookie("NAME").getValue(), "Niall Gallagher");
        AssertJUnit.assertEquals(request.getCookie("NAME").getPath(), "/");
        AssertJUnit.assertEquals(request.getCookie("NAME").getDomain(), null);
        AssertJUnit.assertEquals(list.size(), 4);
        AssertJUnit.assertEquals(list.get(0).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(0).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(0).getHeader("Content-Disposition"), "file; name=\"file1\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
        AssertJUnit.assertEquals(list.get(0).getName(), "file1");
        AssertJUnit.assertEquals(list.get(0).getFileName(), "file1.txt");
        AssertJUnit.assertEquals(list.get(0).isFile(), true);
        AssertJUnit.assertEquals(list.get(0).getContent(), "example contents of file1.txt");
        AssertJUnit.assertEquals(request.getPart("file1").getContent(), "example contents of file1.txt");
        AssertJUnit.assertEquals(list.get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(1).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(1).getHeader("Content-Disposition"), "file; name=\"file2\"; filename=\"file2.txt\"");
        AssertJUnit.assertEquals(list.get(1).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(1).getName(), "file2");
        AssertJUnit.assertEquals(list.get(1).getFileName(), "file2.txt");
        AssertJUnit.assertEquals(list.get(1).isFile(), true);
        AssertJUnit.assertEquals(list.get(1).getContent(), "example contents of file2.txt ...");
        AssertJUnit.assertEquals(request.getPart("file2").getContent(), "example contents of file2.txt ...");
        AssertJUnit.assertEquals(list.get(2).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(2).getHeader("Content-Disposition"), "file; name=\"file3\"; filename=\"file3.txt\"");
        AssertJUnit.assertEquals(list.get(2).getName(), "file3");
        AssertJUnit.assertEquals(list.get(2).getFileName(), "file3.txt");
        AssertJUnit.assertEquals(list.get(2).isFile(), true);
        AssertJUnit.assertEquals(list.get(2).getContent(), "example contents of file3.txt ...");
        AssertJUnit.assertEquals(request.getPart("file3").getContent(), "example contents of file3.txt ...");
        AssertJUnit.assertEquals(list.get(3).getContentType().getPrimary(), "text");
        AssertJUnit.assertEquals(list.get(3).getContentType().getSecondary(), "plain");
        AssertJUnit.assertEquals(list.get(3).getHeader("Content-Disposition"), "file; name=\"file4\"; filename=\"file4.txt\"");
        AssertJUnit.assertEquals(list.get(3).getName(), "file4");
        AssertJUnit.assertEquals(list.get(3).getFileName(), "file4.txt");
        AssertJUnit.assertEquals(list.get(3).isFile(), true);
        AssertJUnit.assertEquals(list.get(3).getContent(), "example contents of file4.txt ...");
        AssertJUnit.assertEquals(request.getPart("file4").getContent(), "example contents of file4.txt ...");
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(request.getContent(), BODY);
    }
}
