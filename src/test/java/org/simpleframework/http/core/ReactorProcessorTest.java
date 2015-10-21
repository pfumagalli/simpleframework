package org.simpleframework.http.core;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.ReactorTest.TestChannel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ReactorProcessorTest implements Container {

    private static final int ITERATIONS = 20000;

    private static final String MINIMAL =
            "HEAD /MINIMAL/%s HTTP/1.0\r\n" +
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "\r\n";

    private static final String SIMPLE =
            "GET /SIMPLE/%s HTTP/1.0\r\n" +
                    "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
                    "   \t\t   image/png;\t\r\n\t"+
                    "   q=1.0,*;q=0.1\r\n"+
                    "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
                    "Host:   some.host.com    \r\n"+
                    "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
                    "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
                    "\r\n";

    private static final String UPLOAD =
            "POST /UPLOAD/%s HTTP/1.0\r\n" +
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
                    "Content-Disposition: file; name=\"pics\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file1.txt\r\n"+
                    "--AaB03x\r\n"+
                    "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"pics\"; filename=\"file2.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file3.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"pics\"; filename=\"file3.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y\r\n"+
                    "Content-Disposition: file; name=\"pics\"; filename=\"file4.txt\"\r\n"+
                    "Content-Type: text/plain\r\n\r\n"+
                    "example contents of file4.txt ...\r\n"+
                    "--BbC04y--\r\n"+
                    "--AaB03x--\r\n";

    private static class StopWatch {

        private long duration;

        private final long start;

        public StopWatch() {
            this.start = System.currentTimeMillis();
        }

        public long time() {
            return duration;
        }

        public void stop() {
            duration = System.currentTimeMillis() - start;
        }
    }

    public static class MockChannel implements Channel {

        private final ByteCursor cursor;

        public MockChannel(StreamCursor cursor, int dribble) {
            this.cursor = new DribbleCursor(cursor, dribble);
        }
        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public Trace getTrace() {
            return new MockTrace();
        }

        @Override
        public Certificate getCertificate() {
            return null;
        }

        @Override
        public ByteCursor getCursor() {
            return cursor;
        }

        @Override
        public ByteWriter getWriter() {
            return null;
        }

        @Override
        public Map getAttributes() {
            return null;
        }

        @Override
        public void close() {}

        @Override
        public SocketChannel getSocket() {
            return null;
        }
    }

    private final ConcurrentHashMap<String, StopWatch> timers = new ConcurrentHashMap<String, StopWatch>();

    private final LinkedBlockingQueue<StopWatch> finished = new LinkedBlockingQueue<StopWatch>();

    @Test
    public void testMinimal() throws Exception {
        final Controller handler = new ContainerController(this, new ArrayAllocator(), 10, 2);

        testRequest(handler, "/MINIMAL/%s", MINIMAL, "MINIMAL");
        testRequest(handler, "/SIMPLE/%s", SIMPLE, "SIMPLE");
        testRequest(handler, "/UPLOAD/%s", UPLOAD, "UPLOAD");
    }

    private void testRequest(Controller handler, String target, String payload, String name) throws Exception {
        final long start = System.currentTimeMillis();

        for(int i = 0; i < ITERATIONS; i++) {
            final String request = String.format(payload, i);
            final StopWatch stopWatch = new StopWatch();

            timers.put(String.format(target, i), stopWatch);
            testHandler(handler, request, 2048);
        }
        double sum = 0;

        for(int i = 0; i < ITERATIONS; i++) {
            final StopWatch stopWatch = finished.take();
            sum += stopWatch.time();
        }
        final double total = (System.currentTimeMillis() - start);
        final double count = ITERATIONS;

        System.err.println(String.format("%s total=[%s] for=[%s] average=[%s] time-per-request=[%s] request-per-millisecond=[%s] request-per-second=[%s]",
                name, total, count, sum / count, total / count, (count / total) + 1, count / (total / 1000)));
    }

    private void testHandler(Controller handler, String payload, int dribble) throws Exception {
        final StreamCursor cursor = new StreamCursor(payload);
        final Channel channel = new TestChannel(cursor, dribble);

        handler.start(channel);
    }


    @Override
    public void handle(Request request, Response response) {
        try {
            process(request, response);
        }catch(final Exception e) {
            e.printStackTrace();
            AssertJUnit.assertTrue(false);
        }
    }

    public void process(Request request, Response response) throws Exception {
        final List<Part> list = request.getParts();
        final String method = request.getMethod();

        if(method.equals("HEAD")) {
            AssertJUnit.assertEquals(request.getMajor(), 1);
            AssertJUnit.assertEquals(request.getMinor(), 0);
            AssertJUnit.assertEquals(request.getValue("Host"), "some.host.com");
        } else if(method.equals("GET")) {
            AssertJUnit.assertEquals(request.getMajor(), 1);
            AssertJUnit.assertEquals(request.getMinor(), 0);
            AssertJUnit.assertEquals(request.getValue("Host"), "some.host.com");
            AssertJUnit.assertEquals(request.getValues("Accept").size(), 4);
            AssertJUnit.assertEquals(request.getValues("Accept").get(0), "image/gif");
            AssertJUnit.assertEquals(request.getValues("Accept").get(1), "image/png");
            AssertJUnit.assertEquals(request.getValues("Accept").get(2), "image/jpeg");
            AssertJUnit.assertEquals(request.getValues("Accept").get(3), "*");
        } else {
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
            AssertJUnit.assertEquals(list.size(), 4);
            AssertJUnit.assertEquals(list.get(0).getContentType().getPrimary(), "text");
            AssertJUnit.assertEquals(list.get(0).getContentType().getSecondary(), "plain");
            AssertJUnit.assertEquals(list.get(0).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
            AssertJUnit.assertEquals(list.get(0).getName(), "pics");
            AssertJUnit.assertEquals(list.get(0).getFileName(), "file1.txt");
            AssertJUnit.assertEquals(list.get(0).isFile(), true);
            AssertJUnit.assertEquals(list.get(1).getContentType().getPrimary(), "text");
            AssertJUnit.assertEquals(list.get(1).getContentType().getSecondary(), "plain");
            AssertJUnit.assertEquals(list.get(1).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file2.txt\"");
            AssertJUnit.assertEquals(list.get(1).getContentType().getPrimary(), "text");
            AssertJUnit.assertEquals(list.get(1).getName(), "pics");
            AssertJUnit.assertEquals(list.get(1).getFileName(), "file2.txt");
            AssertJUnit.assertEquals(list.get(1).isFile(), true);
            AssertJUnit.assertEquals(list.get(2).getContentType().getSecondary(), "plain");
            AssertJUnit.assertEquals(list.get(2).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file3.txt\"");
            AssertJUnit.assertEquals(list.get(2).getName(), "pics");
            AssertJUnit.assertEquals(list.get(2).getFileName(), "file3.txt");
            AssertJUnit.assertEquals(list.get(2).isFile(), true);
            AssertJUnit.assertEquals(list.get(3).getContentType().getPrimary(), "text");
            AssertJUnit.assertEquals(list.get(3).getContentType().getSecondary(), "plain");
            AssertJUnit.assertEquals(list.get(3).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file4.txt\"");
            AssertJUnit.assertEquals(list.get(3).getName(), "pics");
            AssertJUnit.assertEquals(list.get(3).getFileName(), "file4.txt");
            AssertJUnit.assertEquals(list.get(3).isFile(), true);
        }
        final StopWatch stopWatch = timers.get(request.getTarget());
        stopWatch.stop();
        finished.offer(stopWatch);
    }

    public static void main(String[] list) throws Exception {
        new ReactorProcessorTest().testMinimal();
    }
}
