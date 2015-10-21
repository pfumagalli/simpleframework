package org.simpleframework.http.core;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConversationTest {

    private MockRequest request;
    private MockResponse response;
    private Conversation support;

    @BeforeMethod
    public void setUp() {
        request = new MockRequest();
        response = new MockResponse();
        support = new Conversation(request, response);
    }

    @Test
    public void testWebSocket() {
        request.setMajor(1);
        request.setMinor(1);
        response.setValue("Connection", "upgrade");

        AssertJUnit.assertFalse(support.isWebSocket());
        AssertJUnit.assertFalse(support.isTunnel());
        AssertJUnit.assertTrue(support.isKeepAlive());

        request.setValue("Upgrade", "WebSocket");

        AssertJUnit.assertFalse(support.isWebSocket());
        AssertJUnit.assertFalse(support.isTunnel());
        AssertJUnit.assertTrue(support.isKeepAlive());

        response.setCode(101);
        response.setValue("Upgrade", "websocket");

        AssertJUnit.assertTrue(support.isWebSocket());
        AssertJUnit.assertTrue(support.isTunnel());
        AssertJUnit.assertTrue(support.isKeepAlive());
    }

    @Test
    public void testConnectTunnel() {
        request.setMajor(1);
        request.setMinor(1);
        response.setCode(404);
        request.setMethod("CONNECT");

        AssertJUnit.assertFalse(support.isWebSocket());
        AssertJUnit.assertFalse(support.isTunnel());
        AssertJUnit.assertTrue(support.isKeepAlive());

        response.setCode(200);

        AssertJUnit.assertFalse(support.isWebSocket());
        AssertJUnit.assertTrue(support.isTunnel());
        AssertJUnit.assertTrue(support.isKeepAlive());
    }

    @Test
    public void testResponse() {
        request.setMajor(1);
        request.setMinor(1);
        response.setValue("Content-Length", "10");
        response.setValue("Connection", "close");

        AssertJUnit.assertFalse(support.isKeepAlive());
        AssertJUnit.assertTrue(support.isPersistent());
        AssertJUnit.assertEquals(support.getContentLength(), 10);
        AssertJUnit.assertEquals(support.isChunkedEncoded(), false);

        request.setMinor(0);

        AssertJUnit.assertFalse(support.isKeepAlive());
        AssertJUnit.assertFalse(support.isPersistent());

        response.setValue("Connection", "keep-alive");

        AssertJUnit.assertTrue(support.isKeepAlive());
        AssertJUnit.assertFalse(support.isPersistent());

        response.setValue("Transfer-Encoding", "chunked");

        AssertJUnit.assertTrue(support.isChunkedEncoded());
        AssertJUnit.assertTrue(support.isKeepAlive());
    }

    @Test
    public void testConversation() {
        request.setMajor(1);
        request.setMinor(1);
        support.setChunkedEncoded();

        AssertJUnit.assertEquals(response.getValue("Transfer-Encoding"), "chunked");
        AssertJUnit.assertEquals(response.getValue("Connection"), "keep-alive");
        AssertJUnit.assertTrue(support.isKeepAlive());
        AssertJUnit.assertTrue(support.isPersistent());

        request.setMinor(0);
        support.setChunkedEncoded();

        AssertJUnit.assertEquals(response.getValue("Connection"), "close");
        AssertJUnit.assertFalse(support.isKeepAlive());

        request.setMajor(1);
        request.setMinor(1);
        response.setValue("Content-Length", "10");
        response.setValue("Connection", "close");

        AssertJUnit.assertFalse(support.isKeepAlive());
        AssertJUnit.assertTrue(support.isPersistent());
        AssertJUnit.assertEquals(support.getContentLength(), 10);

        request.setMinor(0);

        AssertJUnit.assertFalse(support.isKeepAlive());
        AssertJUnit.assertFalse(support.isPersistent());

        response.setValue("Connection", "keep-alive");

        AssertJUnit.assertTrue(support.isKeepAlive());
        AssertJUnit.assertFalse(support.isPersistent());

        response.setValue("Transfer-Encoding", "chunked");

        AssertJUnit.assertTrue(support.isChunkedEncoded());
        AssertJUnit.assertTrue(support.isKeepAlive());
    }
}
