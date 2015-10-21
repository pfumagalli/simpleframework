package org.simpleframework.http.core;

import java.util.List;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.RequestHeader;

public class MockProxyRequest extends MockRequest {

    private final RequestHeader header;

    public MockProxyRequest(RequestHeader header) {
        this.header = header;
    }

    @Override
    public long getContentLength() {
        return header.getContentLength();
    }

    @Override
    public ContentType getContentType() {
        return header.getContentType();
    }

    @Override
    public String getValue(String name) {
        return header.getValue(name);
    }

    @Override
    public List<String> getValues(String name) {
        return header.getValues(name);
    }

    @Override
    public int getMajor() {
        return header.getMajor();
    }

    @Override
    public String getMethod() {
        return header.getMethod();
    }

    @Override
    public int getMinor() {
        return header.getMajor();
    }

    @Override
    public Path getPath() {
        return header.getPath();
    }

    @Override
    public Query getQuery() {
        return header.getQuery();
    }

    @Override
    public String getTarget() {
        return header.getTarget();
    }


    @Override
    public String getParameter(String name) {
        return header.getQuery().get(name);
    }

    @Override
    public Cookie getCookie(String name) {
        return header.getCookie(name);
    }
}
