package org.simpleframework.http.message;

import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SegmentConsumerPerformanceTest {

    private static final int ITERATIONS = 1000000;

    private static final String SOURCE_1 =
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

    private static final String SOURCE_2 =
            "Host: ajax.googleapis.com\r\n"+
                    "Connection: keep-alive\r\n"+
                    "Pragma: no-cache\r\n"+
                    "Cache-Control: no-cache\r\n"+
                    "Accept: */*\r\n"+
                    "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36\r\n"+
                    "X-Client-Data: CKK2yQEIqbbJAQjEtskBCPCIygEI/ZXKAQi9mMoB\r\n"+
                    "Referer: http://stackoverflow.com/questions/25033458/memory-consumed-by-a-thread\r\n"+
                    "Accept-Encoding: gzip, deflate, sdch\r\n"+
                    "Accept-Language: en-GB,en-US;q=0.8,en;q=0.6\r\n"+
                    "\r\n";


    @Test
    public void testEmptyHeaderComplex() throws Exception {
        byte[] data = SOURCE_1.getBytes("UTF-8");

        for(int i = 0; i < 4; i++) {
            final SegmentConsumer header = new SegmentConsumer();
            final ByteCursor cursor = new StreamCursor(data);

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
        for(int x = 0; x < 4; x++) {
            data = SOURCE_1.getBytes("UTF-8");
            long start = System.currentTimeMillis();

            for(int i = 0; i < ITERATIONS; i++) {
                final SegmentConsumer header = new SegmentConsumer();
                final ByteCursor cursor = new StreamCursor(data);

                while(!header.isFinished()) {
                    header.consume(cursor);
                }
                AssertJUnit.assertEquals(cursor.ready(), -1);
            }
            long finish = System.currentTimeMillis();
            long duration = finish - start;

            System.err.println("SOURCE_1 time: " + duration + " ms, performance: " + (ITERATIONS/duration)+" headers per ms and " + (ITERATIONS/(duration/1000)) + " per second");

            data = SOURCE_2.getBytes("UTF-8");
            start = System.currentTimeMillis();

            for(int i = 0; i < ITERATIONS; i++) {
                final SegmentConsumer header = new SegmentConsumer();
                final ByteCursor cursor = new StreamCursor(data);

                while(!header.isFinished()) {
                    header.consume(cursor);
                }
                AssertJUnit.assertEquals(cursor.ready(), -1);
            }
            finish = System.currentTimeMillis();
            duration = finish - start;

            System.err.println("SOURCE_2 time: " + duration + " ms, performance: " + (ITERATIONS/duration)+" headers per ms and " + (ITERATIONS/(duration/1000)) + " per second");
        }
    }
}
