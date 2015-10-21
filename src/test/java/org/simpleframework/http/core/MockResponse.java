package org.simpleframework.http.core;

import static org.simpleframework.http.Protocol.CLOSE;
import static org.simpleframework.http.Protocol.CONNECTION;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import org.simpleframework.http.Response;

public class MockResponse extends ResponseMessage implements Response {

    private boolean committed;

    public MockResponse() {
        super();
    }

    @Override
    public OutputStream getOutputStream() {
        return System.out;
    }

    @Override
    public boolean isKeepAlive() {
        final String value = getValue(CONNECTION);

        if(value != null) {
            return value.equalsIgnoreCase(CLOSE);
        }
        return true;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void commit() {
        committed = true;
    }

    @Override
    public void reset() {
        return;
    }

    @Override
    public void close() {
        return;
    }

    public Object getAttribute(String name) {
        return null;
    }

    public Map getAttributes() {
        return null;
    }

    @Override
    public OutputStream getOutputStream(int size) throws IOException {
        return null;
    }

    @Override
    public PrintStream getPrintStream() throws IOException {
        return null;
    }

    @Override
    public PrintStream getPrintStream(int size) throws IOException {
        return null;
    }

    @Override
    public void setContentLength(long length) {
        setValue("Content-Length", String.valueOf(length));
    }

    @Override
    public WritableByteChannel getByteChannel() throws IOException {
        return null;
    }

    @Override
    public WritableByteChannel getByteChannel(int size) throws IOException {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getResponseTime() {
        return 0;
    }

    @Override
    public void setContentType(String type) {
        setValue("Content-Type", type);
    }
}
