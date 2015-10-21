package org.simpleframework.http.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.simpleframework.http.Part;


public class MockBody implements Body {

    protected PartData list;

    protected String body;

    public MockBody() {
        this("");
    }

    public MockBody(String body) {
        this.list = new PartData();
        this.body = body;
    }

    @Override
    public List<Part> getParts() {
        return list.getParts();
    }

    @Override
    public Part getPart(String name) {
        return list.getPart(name);
    }

    @Override
    public String getContent(String charset) {
        return body;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body.getBytes("UTF-8"));
    }

    @Override
    public String getContent() throws IOException {
        return body;
    }

}
