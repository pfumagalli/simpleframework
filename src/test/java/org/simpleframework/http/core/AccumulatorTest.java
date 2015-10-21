package org.simpleframework.http.core;

import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class AccumulatorTest {

    @Test
    public void testAccumulator() throws IOException {
        MockChannel channel = new MockChannel(null);
        MockObserver monitor = new MockObserver();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        Conversation support = new Conversation(request, response);
        ResponseBuffer buffer = new ResponseBuffer(monitor, response, support, channel);

        final byte[] content = { 'T', 'E', 'S', 'T' };

        // Start a HTTP/1.1 conversation
        request.setMajor(1);
        request.setMinor(1);

        // Write to a zero capacity buffer
        buffer.expand(0);
        buffer.write(content, 0, content.length);

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), "chunked");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), null);
        AssertJUnit.assertEquals(response.getContentLength(), -1);
        AssertJUnit.assertTrue(response.isCommitted());

        channel = new MockChannel(null);
        monitor = new MockObserver();
        request = new MockRequest();
        response = new MockResponse();
        support = new Conversation(request, response);
        buffer = new ResponseBuffer(monitor, response, support, channel);

        // Start a HTTP/1.0 conversation
        request.setMajor(1);
        request.setMinor(0);

        // Write to a zero capacity buffer
        buffer.expand(0);
        buffer.write(content, 0, content.length);

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getValue("Content-Length"), null);
        AssertJUnit.assertEquals(response.getContentLength(), -1);
        AssertJUnit.assertTrue(response.isCommitted());

        channel = new MockChannel(null);
        monitor = new MockObserver();
        request = new MockRequest();
        response = new MockResponse();
        support = new Conversation(request, response);
        buffer = new ResponseBuffer(monitor, response, support, channel);

        // Start a HTTP/1.1 conversation
        request.setMajor(1);
        request.setMinor(1);

        // Write to a large capacity buffer
        buffer.expand(1024);
        buffer.write(content, 0, content.length);

        AssertJUnit.assertEquals(response.getValue("Connection"), null);
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getValue("Content-Length"), null);
        AssertJUnit.assertEquals(response.getContentLength(), -1);
        AssertJUnit.assertFalse(response.isCommitted());
        AssertJUnit.assertFalse(monitor.isReady());
        AssertJUnit.assertFalse(monitor.isClose());
        AssertJUnit.assertFalse(monitor.isError());

        // Flush the buffer
        buffer.close();

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "4");
        AssertJUnit.assertEquals(response.getContentLength(), 4);
        AssertJUnit.assertTrue(response.isCommitted());
        AssertJUnit.assertTrue(monitor.isReady());
        AssertJUnit.assertFalse(monitor.isClose());
        AssertJUnit.assertFalse(monitor.isError());

        boolean catchOverflow = false;

        try {
            buffer.write(content, 0, content.length);
        } catch(final Exception e) {
            catchOverflow = true;
        }
        AssertJUnit.assertTrue(catchOverflow);
    }
}
