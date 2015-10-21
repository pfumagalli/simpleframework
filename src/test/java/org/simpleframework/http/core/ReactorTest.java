package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.lease.Lease;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Part;
import org.simpleframework.http.message.Body;
import org.simpleframework.http.message.Entity;
import org.simpleframework.http.message.Header;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ReactorTest implements Controller {

    private static final String SOURCE =
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

    public static class TestChannel implements Channel {

        private final ByteCursor cursor;

        public TestChannel(StreamCursor cursor, int dribble) {
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

        public Lease getLease() {
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

    @Test
    public void testHandler() throws Exception {
        testHandler(1024);

        for(int i = 10; i < 2048; i++) {
            testHandler(i);
        }
    }

    private void testHandler(int dribble) throws Exception {
        final StreamCursor cursor = new StreamCursor(SOURCE);
        final Channel channel = new TestChannel(cursor, dribble);

        start(channel);

        AssertJUnit.assertEquals(cursor.ready(), -1);
    }

    @Override
    public void start(Channel channel) throws IOException {
        start(new RequestCollector(new ArrayAllocator(), channel));
    }

    @Override
    public void start(Collector collector) throws IOException {
        collector.collect(this);
    }

    @Override
    public void select(Collector collector) throws IOException {
        collector.collect(this);
    }

    @Override
    public void ready(Collector collector) throws IOException {
        final Entity entity = collector;
        final Channel channel = entity.getChannel();
        final ByteCursor cursor = channel.getCursor();
        final Header header = entity.getHeader();
        final Body body = entity.getBody();
        final List<Part> list = body.getParts();

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
        AssertJUnit.assertEquals(cursor.ready(), -1);
    }

    @Override
    public void stop() throws IOException {}
}
