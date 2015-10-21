package org.simpleframework.http.core;

import org.simpleframework.transport.ByteWriter;


public class MockObserver implements BodyObserver {

    private boolean close;

    private boolean error;

    private boolean ready;

    private boolean commit;

    public MockObserver() {
        super();
    }

    @Override
    public void close(ByteWriter sender) {
        close = true;
    }

    public boolean isClose() {
        return close;
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public void ready(ByteWriter sender) {
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public void error(ByteWriter sender) {
        error = true;
    }

    @Override
    public boolean isClosed() {
        return close || error;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void commit(ByteWriter sender) {
        this.commit = commit;
    }

    @Override
    public boolean isCommitted() {
        return commit;
    }

}
