package org.simpleframework.http.message;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class RequestConsumerTest {

    private static final byte[] SOURCE_1 =
            ("POST /index.html HTTP/1.0\r\n"+
                    "Content-Type: application/x-www-form-urlencoded\r\n"+
                    "Content-Length: 42\r\n"+
                    "Transfer-Encoding: chunked\r\n"+
                    "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
                    "   \t\t   image/png;\t\r\n\t"+
                    "   q=1.0,*;q=0.1\r\n"+
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
                    "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
                    "\r\n").getBytes();

    private static final byte[] SOURCE_2 =
            ("GET /tmp/amazon_files/21lP7I1XB5L.jpg HTTP/1.1\r\n"+
                    "Accept-Encoding: gzip, deflate\r\n"+
                    "Connection: keep-alive\r\n"+
                    "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
                    "Cache-Control: max-age=0\r\n"+
                    "Host: localhost:9090\r\n"+
                    "Accept-Language: en-US\r\n"+
                    "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
                    "Accept: */*\r\n" +
                    "\r\n").getBytes();

    private static final byte[] SOURCE_3 =
            ("GET /tmp/amazon_files/in-your-city-blue-large._V256095983_.gif HTTP/1.1Accept-Encoding: gzip, deflate\r\n"+
                    "Connection: keep-alive\r\n"+
                    "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
                    "Cache-Control: max-age=0\r\n"+
                    "Host: localhost:9090\r\n"+
                    "Accept-Language: en-US\r\n"+
                    "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
                    "Accept: */*\r\n"+
                    "\r\n").getBytes();

    private static final byte[] SOURCE_4 =
            ("GET /tmp/amazon_files/narrowtimer_transparent._V47062518_.gif HTTP/1.1\r\n"+
                    "Accept-Encoding: gzip, deflate\r\n"+
                    "Connection: keep-alive\r\n"+
                    "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
                    "Cache-Control: max-age=0\r\n"+
                    "Host: localhost:9090\r\n"+
                    "Accept-Language: en-US\r\n"+
                    "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
                    "Accept: */*\r\n"+
                    "\r\n").getBytes();

    @Test
    public void testPerformance() throws Exception {
        testPerformance(SOURCE_1, "/index.html");
        testPerformance(SOURCE_2, "/tmp/amazon_files/21lP7I1XB5L.jpg");
        testPerformance(SOURCE_3, "/tmp/amazon_files/in-your-city-blue-large._V256095983_.gif");
        testPerformance(SOURCE_4, "/tmp/amazon_files/narrowtimer_transparent._V47062518_.gif");
    }

    @Test
    public void testPerformance(byte[] request, String path) throws Exception {
        final long start = System.currentTimeMillis();

        for(int i = 0; i < 10000; i++) {
            final RequestConsumer header = new RequestConsumer();
            final ByteCursor cursor = new StreamCursor(request);

            while(!header.isFinished()) {
                header.consume(cursor);
            }

            AssertJUnit.assertEquals(cursor.ready(), -1);
            AssertJUnit.assertEquals(header.getPath().getPath(), path);
        }
        System.err.printf("%s time=%s%n", path, (System.currentTimeMillis() - start));
    }

    @Test
    public void testHeader() throws Exception {
        final long start = System.currentTimeMillis();

        for(int i = 0; i < 10000; i++) {
            final RequestConsumer header = new RequestConsumer();
            final ByteCursor cursor = new StreamCursor(SOURCE_1);

            while(!header.isFinished()) {
                header.consume(cursor);
            }

            AssertJUnit.assertEquals(cursor.ready(), -1);
            AssertJUnit.assertEquals(header.getTarget(), "/index.html");
            AssertJUnit.assertEquals(header.getMethod(), "POST");
            AssertJUnit.assertEquals(header.getMajor(), 1);
            AssertJUnit.assertEquals(header.getMinor(), 0);
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
        System.err.printf("time=%s%n", (System.currentTimeMillis() - start));
    }

    @Test
    public void testDribble() throws Exception {
        final RequestConsumer header = new RequestConsumer();
        final ByteCursor cursor = new DribbleCursor(new StreamCursor(SOURCE_1), 1);

        while(!header.isFinished()) {
            header.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.ready(), -1);
        AssertJUnit.assertEquals(header.getTarget(), "/index.html");
        AssertJUnit.assertEquals(header.getMethod(), "POST");
        AssertJUnit.assertEquals(header.getMajor(), 1);
        AssertJUnit.assertEquals(header.getMinor(), 0);
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
