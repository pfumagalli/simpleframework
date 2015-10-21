package org.simpleframework.http.socket.service;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.http.core.MockRequest;
import org.simpleframework.http.core.MockResponse;
import org.simpleframework.http.socket.Session;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PathRouterTest {

    public static class A implements Service {
        @Override
        public void connect(Session session) {}
    }

    public static class B implements Service {
        @Override
        public void connect(Session session) {}
    }

    public static class C implements Service {
        @Override
        public void connect(Session session) {}
    }

    @Test
    public void testRouter() throws Exception{
        final Map<String, Service> services = new HashMap<String, Service>();

        services.put("/a", new A());
        services.put("/b", new B());

        final PathRouter router = new PathRouter(services, new C());
        final MockRequest request = new MockRequest();
        final MockResponse response = new MockResponse();

        request.setTarget("/a");

        Service service = router.route(request, response);

        AssertJUnit.assertNull(service);

        request.setValue("Sec-WebSocket-Version", "13");
        request.setValue("connection", "upgrade");
        request.setValue("upgrade", "WebSocket");

        service = router.route(request, response);

        AssertJUnit.assertNotNull(service);
        AssertJUnit.assertEquals(service.getClass(), A.class);
        AssertJUnit.assertEquals(response.getValue("Sec-WebSocket-Version"), "13");

        request.setTarget("/c");

        service = router.route(request, response);

        AssertJUnit.assertNotNull(service);
        AssertJUnit.assertEquals(service.getClass(), C.class);
        AssertJUnit.assertEquals(response.getValue("Sec-WebSocket-Version"), "13");

    }

}
