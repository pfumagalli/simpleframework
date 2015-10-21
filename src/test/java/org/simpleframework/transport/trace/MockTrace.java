package org.simpleframework.transport.trace;

public class MockTrace implements Trace{
    @Override
    public void trace(Object event) {}
    @Override
    public void trace(Object event, Object value) {}
}
