package org.simpleframework.http;

import org.simpleframework.transport.trace.Trace;

public class MockTrace implements Trace{
    @Override
    public void trace(Object event) {}
    @Override
    public void trace(Object event, Object value) {}
}
