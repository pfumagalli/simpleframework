package org.simpleframework.http.core;

import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class TransferTest {

    @Test
    public void testTransferEncoding() throws IOException {
        MockChannel channel = new MockChannel(null);
        MockObserver monitor = new MockObserver();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        Conversation support = new Conversation(request, response);
        ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        // Start a HTTP/1.1 conversation
        request.setMajor(1);
        request.setMinor(1);
        transfer.start();

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
        transfer = new ResponseEncoder(monitor, response, support, channel);

        // Start a HTTP/1.0 conversation
        request.setMajor(1);
        request.setMinor(0);
        transfer.start();

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getValue("Content-Length"), null);
        AssertJUnit.assertEquals(response.getContentLength(), -1);
        AssertJUnit.assertTrue(response.isCommitted());
    }

    @Test
    public void testContentLength() throws IOException {
        MockChannel channel = new MockChannel(null);
        MockObserver monitor = new MockObserver();
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        Conversation support = new Conversation(request, response);
        ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        // Start a HTTP/1.1 conversation
        request.setMajor(1);
        request.setMinor(1);
        transfer.start(1024);

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "1024");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 1024);
        AssertJUnit.assertTrue(response.isCommitted());

        channel = new MockChannel(null);
        monitor = new MockObserver();
        request = new MockRequest();
        response = new MockResponse();
        support = new Conversation(request, response);
        transfer = new ResponseEncoder(monitor, response, support, channel);

        // Start a HTTP/1.0 conversation
        request.setMajor(1);
        request.setMinor(0);
        transfer.start(1024);

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "1024");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 1024);
        AssertJUnit.assertTrue(response.isCommitted());

        channel = new MockChannel(null);
        monitor = new MockObserver();
        request = new MockRequest();
        response = new MockResponse();
        support = new Conversation(request, response);
        transfer = new ResponseEncoder(monitor, response, support, channel);

        // Start a HTTP/1.0 conversation
        request.setMajor(1);
        request.setMinor(1);
        response.setValue("Content-Length", "2048");
        response.setValue("Connection", "close");
        response.setValue("Transfer-Encoding", "chunked");
        transfer.start(1024);

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "1024"); // should be 1024
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 1024);
        AssertJUnit.assertTrue(response.isCommitted());
    }

    @Test
    public void  testHeadMethodWithConnectionClose() throws IOException {
        final MockChannel channel = new MockChannel(null);
        final MockObserver monitor = new MockObserver();
        final MockRequest request = new MockRequest();
        final MockResponse response = new MockResponse();
        final Conversation support = new Conversation(request, response);
        final ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        request.setMajor(1);
        request.setMinor(0);
        request.setMethod("HEAD");
        request.setValue("Connection", "keep-alive");
        response.setContentLength(1024);
        response.setValue("Connection", "close");

        transfer.start();

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "1024"); // should be 1024
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 1024);
    }

    @Test
    public void  testHeadMethodWithSomethingWritten() throws IOException {
        final MockChannel channel = new MockChannel(null);
        final MockObserver monitor = new MockObserver();
        final MockRequest request = new MockRequest();
        final MockResponse response = new MockResponse();
        final Conversation support = new Conversation(request, response);
        final ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        request.setMajor(1);
        request.setMinor(1);
        request.setMethod("HEAD");
        request.setValue("Connection", "keep-alive");
        response.setContentLength(1024);

        transfer.start(512);

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "512"); // should be 512
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 512);
    }

    @Test
    public void testHeadMethodWithNoContentLength() throws IOException {
        final MockChannel channel = new MockChannel(null);
        final MockObserver monitor = new MockObserver();
        final MockRequest request = new MockRequest();
        final MockResponse response = new MockResponse();
        final Conversation support = new Conversation(request, response);
        final ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        request.setMajor(1);
        request.setMinor(1);
        request.setMethod("HEAD");
        request.setValue("Connection", "keep-alive");

        transfer.start();

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), null);
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), "chunked");
        AssertJUnit.assertEquals(response.getContentLength(), -1);
    }

    @Test
    public void testHeadMethodWithNoContentLengthAndSomethingWritten() throws IOException {
        final MockChannel channel = new MockChannel(null);
        final MockObserver monitor = new MockObserver();
        final MockRequest request = new MockRequest();
        final MockResponse response = new MockResponse();
        final Conversation support = new Conversation(request, response);
        final ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

        request.setMajor(1);
        request.setMinor(1);
        request.setMethod("HEAD");
        request.setValue("Connection", "keep-alive");

        transfer.start(32);

        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertEquals(response.getValue("Content-Length"), "32");
        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), null);
        AssertJUnit.assertEquals(response.getContentLength(), 32);
    }
}
