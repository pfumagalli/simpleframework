package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Part;
import org.simpleframework.http.message.MockBody;

public class MockPart extends MockBody implements Part {

    private final String name;
    private final boolean file;

    public MockPart(String name, String body, boolean file) {
        super(body);
        this.file = file;
        this.name = name;
    }

    @Override
    public String getContent() throws IOException {
        return body;
    }

    @Override
    public ContentType getContentType() {
        return null;
    }

    public ContentDisposition getDisposition() {
        return null;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFile() {
        return file;
    }

    @Override
    public String getFileName() {
        return null;
    }

}
